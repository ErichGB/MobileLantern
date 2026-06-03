// ====================================================================
// buzzer_commands.h — Módulo dedicado al buzzer pasivo del ESP32
// ====================================================================
// Conexiones módulo buzzer (S/V/G):
//   S  (señal)  → GPIO 27
//   V  (VCC)    → 3.3V
//   G  (GND)    → GND
//
// Comandos Bluetooth soportados:
//   BUZZER:ON            → Tono continuo (auto-apaga tras duración)
//   BUZZER:OFF           → Detener inmediatamente (cancela todo)
//   BUZZER:BEEP          → Un pitido corto (200ms)
//   BUZZER:BEEP:N        → N pitidos cortos (1-10)
//   BUZZER:TONE:WELCOME  → Melodía de bienvenida (Do-Mi-Sol-Do')
//   BUZZER:TONE:GOODBYE  → Melodía de despedida (Do'-Sol-Mi-Do)
//   BUZZER:TONE:NOTIFY   → Chime de notificación (La-Do#')
//   BUZZER:TONE:SUCCESS  → Confirmación de éxito (Si-Mi')
//   BUZZER:TONE:ALERT    → Alerta urgente (alternante)
// ====================================================================

#ifndef BUZZER_COMMANDS_H
#define BUZZER_COMMANDS_H

#include <Arduino.h>
#include "BluetoothSerial.h"

extern BluetoothSerial SerialBT;

// ====== Configuración ======
#define BUZZER_PIN              27
#define BUZZER_DEFAULT_FREQ     1000
#define BUZZER_DEFAULT_DURATION 2000
#define BUZZER_BEEP_ON_MS       200
#define BUZZER_BEEP_OFF_MS      150
#define BUZZER_MAX_BEEPS        10

// ====== Estructura de nota para melodías ======
// freq=0 → silencio (pausa entre notas)
struct ToneNote { uint16_t freq; uint16_t ms; };
#define TONE_LEN(t) (sizeof(t)/sizeof(ToneNote))

// ====== Melodías predefinidas (PROGMEM = almacenadas en flash) ======
// Contexto educativo: diseñadas para retroalimentación en aula

// WELCOME — Acorde mayor ascendente C5→E5→G5→C6 (alegre, invitante)
static const ToneNote TONE_WELCOME[] PROGMEM = {
  {523,150},{0,30},{659,150},{0,30},{784,150},{0,30},{1047,300}
};
// GOODBYE — Acorde mayor descendente C6→G5→E5→C5 (suave, cierre)
static const ToneNote TONE_GOODBYE[] PROGMEM = {
  {1047,150},{0,30},{784,150},{0,30},{659,150},{0,30},{523,300}
};
// NOTIFY — Chime de dos tonos A5→C#6 (neutro, atención)
static const ToneNote TONE_NOTIFY[] PROGMEM = {
  {880,120},{0,60},{1109,250}
};
// SUCCESS — Coin sound B5→E6 (logro, recompensa)
static const ToneNote TONE_SUCCESS[] PROGMEM = {
  {988,100},{0,30},{1319,350}
};
// ALERT — Alternante 1.5k/1.2k (urgencia, atención inmediata)
static const ToneNote TONE_ALERT[] PROGMEM = {
  {1500,150},{0,50},{1200,150},{0,50},{1500,150},{0,50},{1200,150}
};
// ====== Estado del buzzer ======
uint16_t buzzerFreq     = BUZZER_DEFAULT_FREQ;
uint16_t buzzerDuration = BUZZER_DEFAULT_DURATION;
bool     buzzerActive   = false;
unsigned long buzzerStartTime = 0;

// Patrón de beeps
uint8_t  buzzerBeepTotal   = 0;
uint8_t  buzzerBeepCurrent = 0;
bool     buzzerBeepPhase   = false;
unsigned long buzzerBeepTimer = 0;
bool     buzzerBeeping     = false;

// Reproductor de melodías (no-bloqueante)
const ToneNote* toneSeq = nullptr;
uint8_t  toneLen     = 0;
uint8_t  toneIdx     = 0;
unsigned long toneTimer = 0;
bool     tonePlaying = false;

// ====== Funciones del buzzer ======

void buzzerInit() {
  ledcAttach(BUZZER_PIN, BUZZER_DEFAULT_FREQ, 8);
  ledcWriteTone(BUZZER_PIN, 0);
  Serial.println("[HW] Buzzer inicializado en GPIO " + String(BUZZER_PIN));
}

void buzzerStop() {
  ledcWriteTone(BUZZER_PIN, 0);
  buzzerActive = false;
  buzzerBeeping = false;
  tonePlaying = false;
  buzzerBeepTotal = 0;
  buzzerBeepCurrent = 0;
  Serial.println("[HW] 🔕 Buzzer OFF");
}

void buzzerStart() {
  buzzerBeeping = false;
  tonePlaying = false;
  ledcWriteTone(BUZZER_PIN, buzzerFreq);
  buzzerActive = true;
  buzzerStartTime = millis();
  Serial.printf("[HW] 🔔 Buzzer ON (%d Hz, %d ms)\n", buzzerFreq, buzzerDuration);
}

void buzzerBeep(uint8_t count) {
  if (count == 0) count = 1;
  if (count > BUZZER_MAX_BEEPS) count = BUZZER_MAX_BEEPS;
  buzzerActive = false;
  tonePlaying = false;
  buzzerBeeping = true;
  buzzerBeepTotal = count;
  buzzerBeepCurrent = 0;
  buzzerBeepPhase = true;
  ledcWriteTone(BUZZER_PIN, buzzerFreq);
  buzzerBeepTimer = millis();
  Serial.printf("[HW] 🔔 Buzzer BEEP x%d (%d Hz)\n", count, buzzerFreq);
}

/**
 * Inicia la reproducción de una melodía almacenada en PROGMEM.
 * No-bloqueante: avanza nota a nota en buzzerUpdate().
 */
void buzzerPlayTone(const ToneNote* seq, uint8_t len) {
  buzzerActive = false;
  buzzerBeeping = false;
  toneSeq = seq;
  toneLen = len;
  toneIdx = 0;
  tonePlaying = true;
  uint16_t f = pgm_read_word(&seq[0].freq);
  ledcWriteTone(BUZZER_PIN, f);
  toneTimer = millis();
}

/**
 * Actualiza el estado del buzzer. Llamar en cada iteración de loop().
 * Gestiona: tono continuo, patrón de beeps y reproductor de melodías.
 */
void buzzerUpdate() {
  // --- Reproductor de melodías (prioridad sobre beeps/tonos) ---
  if (tonePlaying) {
    if (millis() - toneTimer >= pgm_read_word(&toneSeq[toneIdx].ms)) {
      toneIdx++;
      if (toneIdx >= toneLen) {
        ledcWriteTone(BUZZER_PIN, 0);
        tonePlaying = false;
        Serial.println("[HW] 🎵 Melodía completada");
      } else {
        uint16_t f = pgm_read_word(&toneSeq[toneIdx].freq);
        ledcWriteTone(BUZZER_PIN, f);
        toneTimer = millis();
      }
    }
    return;  // No procesar beeps/tonos mientras suena melodía
  }

  // --- Tono continuo: auto-apagado ---
  if (buzzerActive && !buzzerBeeping) {
    if (millis() - buzzerStartTime >= buzzerDuration) {
      ledcWriteTone(BUZZER_PIN, 0);
      buzzerActive = false;
      Serial.println("[HW] 🔕 Buzzer auto-off");
    }
  }

  // --- Patrón de beeps ---
  if (buzzerBeeping) {
    unsigned long now = millis();
    if (buzzerBeepPhase) {
      if (now - buzzerBeepTimer >= BUZZER_BEEP_ON_MS) {
        ledcWriteTone(BUZZER_PIN, 0);
        buzzerBeepCurrent++;
        if (buzzerBeepCurrent >= buzzerBeepTotal) {
          buzzerBeeping = false;
          Serial.println("[HW] 🔕 Beep completado");
        } else {
          buzzerBeepPhase = false;
          buzzerBeepTimer = now;
        }
      }
    } else {
      if (now - buzzerBeepTimer >= BUZZER_BEEP_OFF_MS) {
        ledcWriteTone(BUZZER_PIN, buzzerFreq);
        buzzerBeepPhase = true;
        buzzerBeepTimer = now;
      }
    }
  }
}

// ====== Procesador de comandos BUZZER:* ======

bool processBuzzerCommand(String cmd) {
  if (!cmd.startsWith("BUZZER:")) return false;

  String sub = cmd.substring(7);

  if (sub == "ON") {
    BuzzerCmd bcmd = {};
    bcmd.type = BUZZER_CMD_ON;
    xQueueSend(buzzerQueue, &bcmd, 0);
    SerialBT.println("OK:BUZZER:ON");
  }
  else if (sub == "OFF") {
    BuzzerCmd bcmd = {};
    bcmd.type = BUZZER_CMD_OFF;
    xQueueSend(buzzerQueue, &bcmd, 0);
    SerialBT.println("OK:BUZZER:OFF");
  }
  else if (sub == "BEEP") {
    BuzzerCmd bcmd = {};
    bcmd.type = BUZZER_CMD_BEEP;
    bcmd.beepCount = 1;
    xQueueSend(buzzerQueue, &bcmd, 0);
    SerialBT.println("OK:BUZZER:BEEP:1");
  }
  else if (sub.startsWith("BEEP:")) {
    int count = sub.substring(5).toInt();
    count = constrain(count, 1, BUZZER_MAX_BEEPS);
    BuzzerCmd bcmd = {};
    bcmd.type = BUZZER_CMD_BEEP;
    bcmd.beepCount = (uint8_t)count;
    xQueueSend(buzzerQueue, &bcmd, 0);
    SerialBT.println("OK:BUZZER:BEEP:" + String(count));
  }
  else if (sub.startsWith("TONE:")) {
    String name = sub.substring(5);
    name.toUpperCase();

    BuzzerCmd bcmd = {};
    bcmd.type = BUZZER_CMD_TONE;
    bool found = true;

    if      (name == "WELCOME") bcmd.tone = TONE_ID_WELCOME;
    else if (name == "GOODBYE") bcmd.tone = TONE_ID_GOODBYE;
    else if (name == "NOTIFY")  bcmd.tone = TONE_ID_NOTIFY;
    else if (name == "SUCCESS") bcmd.tone = TONE_ID_SUCCESS;
    else if (name == "ALERT")   bcmd.tone = TONE_ID_ALERT;
    else found = false;

    if (found) {
      xQueueSend(buzzerQueue, &bcmd, 0);
      SerialBT.println("OK:BUZZER:TONE:" + name);
      Serial.println("[HW] 🎵 Tono: " + name);
    } else {
      SerialBT.println("ERROR:UNKNOWN_TONE");
      Serial.println("[HW] ⚠️  Tono desconocido: " + name);
    }
  }
  else {
    SerialBT.println("ERROR:UNKNOWN_BUZZER_CMD");
    Serial.println("[HW] ⚠️  Cmd buzzer desconocido: " + sub);
  }

  return true;
}

#endif // BUZZER_COMMANDS_H

