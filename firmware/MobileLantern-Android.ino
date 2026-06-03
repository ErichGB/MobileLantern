// Connected to ESP32 on /dev/cu.usbserial-210
// Chip type: ESP32-D0WD-V3 (revision v3.1)
// Features: Wi-Fi, BT, Dual Core + LP Core, 240MHz
// Crystal frequency: 40MHz
// LED Ring: WCMCU-2812B-8 (8x WS2812B)
//
// ====== CONFIGURACIÓN ARDUINO IDE (obligatoria) ======
//   Board:            ESP32 Dev Module
//   Partition Scheme: Minimal SPIFFS (1.9MB APP with OTA / 190KB SPIFFS)  ← ¡IMPORTANTE!
//   Upload Speed:     921600
//   Flash Size:       4MB (32Mb)
//
// ⚠️  El firmware usa BT Classic + WiFi + HTTPS + OTA (~1.8 MB).
//     Con el esquema por defecto (1.3 MB) NO cabe.
//     Cambiar en: Tools → Partition Scheme → "Minimal SPIFFS (1.9MB APP with OTA)"
// =====================================================
// 
// Deep Link de ejemplo (generado en tiempo de ejecución):
// mobilelantern://connect?device=ML-XXXX&mac=XX:XX:XX:XX:XX:XX

#include "BluetoothSerial.h"
#include "freertos_tasks.h"
#include "led_commands.h"
#include "buzzer_commands.h"
#include "device_commands.h"
#include "ota_commands.h"



// ====== Bluetooth Classic ======
BluetoothSerial SerialBT;

// Estado anterior para detectar cambios
bool wasConnected = false;


// ====== Callback Bluetooth ======
void btCallback(esp_spp_cb_event_t event, esp_spp_cb_param_t *param) {
  if (event == ESP_SPP_SRV_OPEN_EVT) {
    Serial.println("[BT] ✅ Cliente Android conectado (callback)");
    wasConnected = true;
    if (ledQueue) {
      LedCmd lcmd = {};
      lcmd.type = LED_CMD_OFF;
      xQueueSend(ledQueue, &lcmd, 0);
    }
  }
  else if (event == ESP_SPP_CLOSE_EVT) {
    Serial.println("[BT] ❌ Cliente desconectado (callback)");
    wasConnected = false;
    if (ledQueue) {
      LedCmd lcmd = {};
      lcmd.type = LED_CMD_BLINK;
      xQueueSend(ledQueue, &lcmd, 0);
    }
    if (buzzerQueue) {
      BuzzerCmd bcmd = {};
      bcmd.type = BUZZER_CMD_STOP;
      xQueueSend(buzzerQueue, &bcmd, 0);
    }
    if (otaState == ML_OTA_BT_RECEIVING) {
      otaCancel();
    }
  }
}

// ====== Tarea FreeRTOS: anillo LED ======
void vLedTask(void* param) {
  LedCmd cmd;
  bool blinking = true;
  bool active = false;
  unsigned long lastBlink = 0;
  bool blinkState = false;

  for (;;) {
    if (xQueueReceive(ledQueue, &cmd, pdMS_TO_TICKS(10)) == pdTRUE) {
      switch (cmd.type) {
        case LED_CMD_ON:
          ledMode = MANUAL;
          manualLedState = true;
          currentEffect = NONE;
          blinking = false;
          active = true;
          ring.setBrightness(ringBrightness);
          ringOn();
          break;

        case LED_CMD_OFF:
          ledMode = MANUAL;
          manualLedState = false;
          currentEffect = NONE;
          blinking = false;
          active = false;
          ringOff();
          break;

        case LED_CMD_COLOR:
          ringColor = ring.Color(cmd.r, cmd.g, cmd.b);
          ledMode = MANUAL;
          manualLedState = true;
          blinking = false;
          active = true;
          if (currentEffect == NONE) {
            ring.setBrightness(ringBrightness);
            ringOn();
          }
          break;

        case LED_CMD_BRIGHTNESS:
          ringBrightness = cmd.brightness;
          ring.setBrightness(ringBrightness);
          if (active && currentEffect == NONE) {
            ringOn();
          }
          break;

        case LED_CMD_EFFECT:
          currentEffect = (Effect)cmd.effect;
          effectStep = 0;
          lastEffectUpdate = 0;
          ledMode = MANUAL;
          manualLedState = true;
          blinking = false;
          active = true;
          ring.setBrightness(ringBrightness);
          if (currentEffect == NONE) {
            ringOn();
          }
          break;

        case LED_CMD_BLINK:
          ledMode = AUTO;
          currentEffect = NONE;
          active = false;
          blinking = true;
          ringOff();
          break;

        case LED_CMD_STOP:
          ledMode = AUTO;
          currentEffect = NONE;
          active = false;
          blinking = false;
          ringOff();
          break;
      }
    }

    if (active && currentEffect != NONE) {
      runEffect();
    }

    if (blinking) {
      unsigned long now = millis();
      if (now - lastBlink >= 500) {
        lastBlink = now;
        blinkState = !blinkState;
        ringBlink(blinkState);
      }
    }
  }
}

// ====== Tarea FreeRTOS: buzzer pasivo ======
void vBuzzerTask(void* param) {
  BuzzerCmd cmd;

  for (;;) {
    if (xQueueReceive(buzzerQueue, &cmd, pdMS_TO_TICKS(10)) == pdTRUE) {
      switch (cmd.type) {
        case BUZZER_CMD_ON:
          buzzerStart();
          break;

        case BUZZER_CMD_OFF:
        case BUZZER_CMD_STOP:
          buzzerStop();
          break;

        case BUZZER_CMD_BEEP:
          buzzerBeep(cmd.beepCount);
          break;

        case BUZZER_CMD_TONE:
          switch (cmd.tone) {
            case TONE_ID_WELCOME: buzzerPlayTone(TONE_WELCOME, TONE_LEN(TONE_WELCOME)); break;
            case TONE_ID_GOODBYE: buzzerPlayTone(TONE_GOODBYE, TONE_LEN(TONE_GOODBYE)); break;
            case TONE_ID_NOTIFY:  buzzerPlayTone(TONE_NOTIFY,  TONE_LEN(TONE_NOTIFY));  break;
            case TONE_ID_SUCCESS: buzzerPlayTone(TONE_SUCCESS, TONE_LEN(TONE_SUCCESS)); break;
            case TONE_ID_ALERT:   buzzerPlayTone(TONE_ALERT,   TONE_LEN(TONE_ALERT));   break;
          }
          break;
      }
    }
    buzzerUpdate();
  }
}

// ====== Inicialización de tareas FreeRTOS ======
void tasksInit() {
  ledQueue = xQueueCreate(CMD_QUEUE_DEPTH, sizeof(LedCmd));
  buzzerQueue = xQueueCreate(CMD_QUEUE_DEPTH, sizeof(BuzzerCmd));
  stateMutex = xSemaphoreCreateMutex();

  xTaskCreatePinnedToCore(vLedTask, "led", LED_TASK_STACK, NULL, LED_TASK_PRIO, NULL, 1);
  xTaskCreatePinnedToCore(vBuzzerTask, "buzzer", BUZZER_TASK_STACK, NULL, BUZZER_TASK_PRIO, NULL, 1);

  Serial.println("[RTOS] Tareas LED y Buzzer creadas en Core 1");
}

void setup() {

  ledInit();
  buzzerInit();

  Serial.begin(115200);
  delay(100);

  String btName = deviceInit();
  otaInit();

  if (!SerialBT.begin(btName)) {
    Serial.println("[BT] Error al iniciar Bluetooth!");
    while(1);
  }

  String macAddress = SerialBT.getBtAddressString();
  SerialBT.register_callback(btCallback);

  Serial.println("[BT] ═══════════════════════════════════════════════");
  Serial.println("[BT] Bluetooth Classic iniciado: " + btName);
  Serial.println("[BT] Dirección MAC: " + macAddress);
  Serial.println("[BT] -------------------------------------------------");
  Serial.println("[HW] Anillo LED: WCMCU-2812B-8 (8 LEDs WS2812B)");
  Serial.println("[HW] Pin DIN: GPIO " + String(RING_PIN));
  Serial.println("[HW] Brillo: " + String(DEFAULT_BRIGHTNESS) + "/255");
  Serial.println("[HW] Buzzer pasivo: GPIO " + String(BUZZER_PIN) + " (comandos BUZZER:*)");
  Serial.println("[BT] -------------------------------------------------");
  Serial.println("[BT] Deep Link URL:");
  Serial.println("[BT] mobilelantern://connect?device=" + btName + "&mac=" + macAddress);
  Serial.println("[BT] ------------------------------------------------");
  Serial.println("[BT] Comandos disponibles:");
  Serial.println("[BT]   LED_ON / LED_OFF");
  Serial.println("[BT]   COLOR:R,G,B       (ej: COLOR:255,0,0)");
  Serial.println("[BT]   BRIGHTNESS:N      (0-255)");
  Serial.println("[BT]   EFFECT:RAINBOW / PULSE / SPIN / BREATHE / NONE");
  Serial.println("[BT]   BUZZER:ON/OFF/BEEP/BEEP:N");
  Serial.println("[BT]   BUZZER:TONE:WELCOME/GOODBYE/NOTIFY/SUCCESS/ALERT");
  Serial.println("[BT]   VERSION / DEVICE_INFO / NAME:{nuevo}");
  Serial.println("[BT]   OTA_WIFI:SSID,PASS");
  Serial.println("[BT]   OTA_HTTP_WIFI:SSID,PASS,URL");
  Serial.println("[BT]   OTA_BT / OTA_STATUS / OTA_CANCEL");
  Serial.println("[BT] ═══════════════════════════════════════════════");
  Serial.println();

  ledStartupAnimation();

  tasksInit();
}

// ====== Procesar un comando individual ======
void processCommand(String cmd) {
  Serial.print("[BT] ▶ CMD: ");
  Serial.println(cmd);

  if (processDeviceCommand(cmd)) return;
  if (processOtaCommand(cmd)) return;
  if (processLedCommand(cmd)) return;
  if (processBuzzerCommand(cmd)) return;

  SerialBT.println("ERROR:UNKNOWN_COMMAND");
  Serial.println("[BT] ⚠️  Comando desconocido: " + cmd);
}

void loop() {
  bool isConnected = SerialBT.connected();

  if (isConnected != wasConnected) {
    if (isConnected) {
      Serial.println("[BT] 🔗 Cliente conectado (polling)");
      LedCmd lcmd = {};
      lcmd.type = LED_CMD_OFF;
      xQueueSend(ledQueue, &lcmd, 0);
    } else {
      Serial.println("[BT] 🔌 Cliente desconectado (polling)");
      LedCmd lcmd = {};
      lcmd.type = LED_CMD_BLINK;
      xQueueSend(ledQueue, &lcmd, 0);
      BuzzerCmd bcmd = {};
      bcmd.type = BUZZER_CMD_STOP;
      xQueueSend(buzzerQueue, &bcmd, 0);
      if (otaState == ML_OTA_BT_RECEIVING) {
        otaCancel();
      }
    }
    wasConnected = isConnected;
  }

  otaLoop();

  if (isConnected) {
    if (otaState == ML_OTA_BT_RECEIVING) {
      otaBtUpdate();
      return;
    }

    if (isOtaActive()) {
      if (SerialBT.available()) {
        String received = "";
        while (SerialBT.available()) {
          char c = SerialBT.read();
          received += c;
          delay(1);
        }
        received.trim();
        if (received == "OTA_STATUS" || received == "OTA_CANCEL") {
          processCommand(received);
        }
      }
      return;
    }

    if (SerialBT.available()) {
      String received = "";
      while (SerialBT.available()) {
        char c = SerialBT.read();
        received += c;
        delay(1);
      }

      received.trim();
      Serial.print("[BT] 📥 Recibido: ");
      Serial.println(received);

      int startIdx = 0;
      while (startIdx <= (int)received.length()) {
        int endIdx = received.indexOf('\n', startIdx);
        if (endIdx < 0) endIdx = received.length();
        String cmd = received.substring(startIdx, endIdx);
        cmd.trim();
        if (cmd.length() > 0) {
          processCommand(cmd);
        }
        startIdx = endIdx + 1;
      }
    }
  }
}
