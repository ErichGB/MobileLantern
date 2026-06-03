// ====================================================================
// led_commands.h — Módulo dedicado al anillo LED WS2812B del ESP32
// ====================================================================
// Conexiones WCMCU-2812B-8:
//   DIN  → GPIO 13
//   VCC  → 5V  (o 3.3V con brillo reducido)
//   GND  → GND
//
// Comandos Bluetooth soportados:
//   LED_ON             → Solo prueba: enciende blanco
//   LED_OFF            → Apaga el anillo y desactiva efectos
//   COLOR:R,G,B        → Establece color (activa LED automáticamente)
//   BRIGHTNESS:N       → Ajusta brillo global (0-255)
//   EFFECT:RAINBOW     → Arcoíris rotatorio
//   EFFECT:PULSE       → Pulso sinusoidal
//   EFFECT:SPIN        → LED giratorio con estela
//   EFFECT:BREATHE     → Respiración
//   EFFECT:NONE        → Desactiva efectos (color sólido)
// ====================================================================

#ifndef LED_COMMANDS_H
#define LED_COMMANDS_H

#include <Arduino.h>
#include <Adafruit_NeoPixel.h>
#include "BluetoothSerial.h"

// Referencia al objeto SerialBT definido en el .ino principal
extern BluetoothSerial SerialBT;

// ====== Configuración del anillo LED ======
#define RING_PIN           13      // GPIO para DIN del anillo
#define RING_LEDS          8       // Número de LEDs en el anillo WCMCU-2812B-8
#define DEFAULT_BRIGHTNESS 150     // Brillo por defecto (0-255)

// ====== Objeto NeoPixel ======
Adafruit_NeoPixel ring(RING_LEDS, RING_PIN, NEO_GRB + NEO_KHZ800);

// ====== Control de LED: manual vs automático ======
enum LedMode { AUTO, MANUAL };
LedMode ledMode = AUTO;
bool manualLedState = false;

// ====== Estado del anillo LED ======
uint8_t  ringBrightness = DEFAULT_BRIGHTNESS;
uint32_t ringColor      = 0xFFFFFF;  // Blanco por defecto

// ====== Efectos ======
enum Effect { NONE, RAINBOW, PULSE, SPIN, BREATHE };
Effect currentEffect = NONE;
unsigned long lastEffectUpdate = 0;
uint16_t effectStep = 0;

// ====== Funciones del anillo LED ======

/**
 * Establece todos los LEDs del anillo a un color y actualiza.
 */
void setRingColor(uint32_t color) {
  for (int i = 0; i < RING_LEDS; i++) {
    ring.setPixelColor(i, color);
  }
  ring.show();
}

/**
 * Enciende el anillo con el color y brillo actuales.
 */
void ringOn() {
  ring.setBrightness(ringBrightness);
  setRingColor(ringColor);
}

/**
 * Apaga el anillo (todos los LEDs a negro).
 */
void ringOff() {
  setRingColor(0);
}

// ====== Efectos visuales ======

/**
 * Efecto arcoíris rotatorio multicolor.
 */
void effectRainbow() {
  unsigned long now = millis();
  if (now - lastEffectUpdate < 20) return;
  lastEffectUpdate = now;

  for (int i = 0; i < RING_LEDS; i++) {
    uint16_t hue = (effectStep + (i * 65536 / RING_LEDS)) % 65536;
    ring.setPixelColor(i, ring.gamma32(ring.ColorHSV(hue)));
  }
  ring.show();
  effectStep += 256;
}

/**
 * Efecto pulso: respira con el color actual (onda sinusoidal).
 */
void effectPulse() {
  unsigned long now = millis();
  if (now - lastEffectUpdate < 15) return;
  lastEffectUpdate = now;

  float breath = (exp(sin(effectStep / 100.0 * PI)) - 0.36787944) * 108.0;
  uint8_t b = (uint8_t)constrain(breath, 0, 255);
  ring.setBrightness(b);
  setRingColor(ringColor);
  effectStep++;
  if (effectStep >= 200) effectStep = 0;
}

/**
 * Efecto giro: LED que recorre el anillo con estela degradada.
 */
void effectSpin() {
  unsigned long now = millis();
  if (now - lastEffectUpdate < 80) return;
  lastEffectUpdate = now;

  for (int i = 0; i < RING_LEDS; i++) {
    if (i == (effectStep % RING_LEDS)) {
      ring.setPixelColor(i, ringColor);
    } else {
      uint8_t dist = ((effectStep % RING_LEDS) - i + RING_LEDS) % RING_LEDS;
      if (dist == 1) {
        ring.setPixelColor(i, ring.Color(
          ((ringColor >> 16) & 0xFF) / 4,
          ((ringColor >> 8) & 0xFF) / 4,
          (ringColor & 0xFF) / 4
        ));
      } else if (dist == 2) {
        ring.setPixelColor(i, ring.Color(
          ((ringColor >> 16) & 0xFF) / 16,
          ((ringColor >> 8) & 0xFF) / 16,
          (ringColor & 0xFF) / 16
        ));
      } else {
        ring.setPixelColor(i, 0);
      }
    }
  }
  ring.show();
  effectStep++;
}

/**
 * Efecto respirar: todos los LEDs suben y bajan de brillo simultáneamente.
 */
void effectBreathe() {
  unsigned long now = millis();
  if (now - lastEffectUpdate < 30) return;
  lastEffectUpdate = now;

  uint8_t b = (uint8_t)(ringBrightness * (0.5 + 0.5 * sin(effectStep * 0.05)));
  ring.setBrightness(b);
  setRingColor(ringColor);
  effectStep++;
}

/**
 * Ejecuta el efecto activo en el ciclo actual del loop.
 */
void runEffect() {
  switch (currentEffect) {
    case RAINBOW:  effectRainbow(); break;
    case PULSE:    effectPulse();   break;
    case SPIN:     effectSpin();    break;
    case BREATHE:  effectBreathe(); break;
    default: break;
  }
}

/**
 * Parpadeo azul tenue del anillo (indicador de no-conexión).
 */
void ringBlink(bool state) {
  if (state) {
    ring.setBrightness(40);
    for (int i = 0; i < RING_LEDS; i++) {
      ring.setPixelColor(i, ring.Color(0, 0, 80));
    }
  } else {
    for (int i = 0; i < RING_LEDS; i++) {
      ring.setPixelColor(i, 0);
    }
  }
  ring.show();
}

// ====== Utilidades ======

/**
 * Parsea un color en formato "R,G,B" (ej: "255,0,128").
 * @return true si el formato es válido, false en caso contrario
 */
bool parseColor(String str, uint8_t &r, uint8_t &g, uint8_t &b) {
  int idx1 = str.indexOf(',');
  int idx2 = str.indexOf(',', idx1 + 1);
  if (idx1 < 0 || idx2 < 0) return false;
  r = (uint8_t)str.substring(0, idx1).toInt();
  g = (uint8_t)str.substring(idx1 + 1, idx2).toInt();
  b = (uint8_t)str.substring(idx2 + 1).toInt();
  return true;
}

// ====== Funciones de ciclo de vida ======

/**
 * Inicializa el anillo LED. Llamar una vez en setup().
 */
void ledInit() {
  ring.begin();
  ring.setBrightness(DEFAULT_BRIGHTNESS);
  ring.show();
  Serial.println("[HW] Anillo LED inicializado: " + String(RING_LEDS) + " LEDs en GPIO " + String(RING_PIN));
}

/**
 * Animación de inicio: arcoíris rápido.
 * Llamar al final de setup() tras configurar Serial.
 */
void ledStartupAnimation() {
  for (int j = 0; j < 256; j += 4) {
    for (int i = 0; i < RING_LEDS; i++) {
      ring.setPixelColor(i, ring.gamma32(ring.ColorHSV((j * 256 + i * 65536 / RING_LEDS) % 65536)));
    }
    ring.setBrightness(60);
    ring.show();
    delay(5);
  }
  ringOff();
}

/**
 * Resetea el estado del LED a valores iniciales.
 * Llamar al desconectar Bluetooth.
 */
void ledReset() {
  ledMode = AUTO;
  currentEffect = NONE;
  ringOff();
}

// ====== Procesador de comandos LED ======

/**
 * Procesa comandos LED_ON, LED_OFF, COLOR:, BRIGHTNESS:, EFFECT:.
 * @param cmd Comando completo (ej: "COLOR:255,0,0", "EFFECT:PULSE")
 * @return true si el comando fue reconocido como LED/COLOR/BRIGHTNESS/EFFECT, false si no
 */
bool processLedCommand(String cmd) {

  if (cmd == "LED_ON") {
    LedCmd lcmd = {};
    lcmd.type = LED_CMD_ON;
    xQueueSend(ledQueue, &lcmd, 0);
    SerialBT.println("OK:LED_ON");
    Serial.println("[BT] 💡 Anillo LED encendido (blanco, solo prueba)");
    return true;
  }

  if (cmd == "LED_OFF") {
    LedCmd lcmd = {};
    lcmd.type = LED_CMD_OFF;
    xQueueSend(ledQueue, &lcmd, 0);
    SerialBT.println("OK:LED_OFF");
    Serial.println("[BT] 💡 Anillo LED apagado");
    return true;
  }

  if (cmd.startsWith("COLOR:")) {
    String colorStr = cmd.substring(6);
    uint8_t r, g, b;
    if (parseColor(colorStr, r, g, b)) {
      LedCmd lcmd = {};
      lcmd.type = LED_CMD_COLOR;
      lcmd.r = r;
      lcmd.g = g;
      lcmd.b = b;
      xQueueSend(ledQueue, &lcmd, 0);
      SerialBT.println("OK:COLOR:" + String(r) + "," + String(g) + "," + String(b));
      Serial.printf("[BT] 🎨 Color: R=%d G=%d B=%d\n", r, g, b);
    } else {
      SerialBT.println("ERROR:INVALID_COLOR");
      Serial.println("[BT] ⚠️  Formato de color inválido (usar R,G,B)");
    }
    return true;
  }

  if (cmd.startsWith("BRIGHTNESS:")) {
    int val = cmd.substring(11).toInt();
    LedCmd lcmd = {};
    lcmd.type = LED_CMD_BRIGHTNESS;
    lcmd.brightness = (uint8_t)constrain(val, 0, 255);
    xQueueSend(ledQueue, &lcmd, 0);
    SerialBT.println("OK:BRIGHTNESS:" + String(lcmd.brightness));
    Serial.printf("[BT] 🔆 Brillo: %d/255\n", lcmd.brightness);
    return true;
  }

  if (cmd.startsWith("EFFECT:")) {
    String effectName = cmd.substring(7);
    effectName.toUpperCase();

    LedCmd lcmd = {};
    lcmd.type = LED_CMD_EFFECT;

    if (effectName == "RAINBOW") {
      lcmd.effect = RAINBOW;
      SerialBT.println("OK:EFFECT:RAINBOW");
      Serial.println("[BT] 🌈 Efecto: Arcoíris");
    }
    else if (effectName == "PULSE") {
      lcmd.effect = PULSE;
      SerialBT.println("OK:EFFECT:PULSE");
      Serial.println("[BT] 💫 Efecto: Pulso");
    }
    else if (effectName == "SPIN") {
      lcmd.effect = SPIN;
      SerialBT.println("OK:EFFECT:SPIN");
      Serial.println("[BT] 🔄 Efecto: Giro");
    }
    else if (effectName == "BREATHE") {
      lcmd.effect = BREATHE;
      SerialBT.println("OK:EFFECT:BREATHE");
      Serial.println("[BT] 🫁 Efecto: Respirar");
    }
    else if (effectName == "NONE") {
      lcmd.effect = NONE;
      SerialBT.println("OK:EFFECT:NONE");
      Serial.println("[BT] ⏹️  Sin efecto (color sólido)");
    }
    else {
      SerialBT.println("ERROR:UNKNOWN_EFFECT");
      Serial.println("[BT] ⚠️  Efecto desconocido: " + effectName);
      return true;
    }
    xQueueSend(ledQueue, &lcmd, 0);
    return true;
  }

  return false;
}

#endif // LED_COMMANDS_H

