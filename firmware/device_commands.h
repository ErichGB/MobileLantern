// ====================================================================
// device_commands.h — Módulo de identidad y metadatos del dispositivo
// ====================================================================
// Gestiona:
//   - Nombre del dispositivo con prefijo ML- (inmutable)
//   - Persistencia del nombre personalizado en NVS
//   - Versión del firmware
//   - Información del hardware
//
// Comandos Bluetooth soportados:
//   VERSION              → Responde OK:VERSION:X.X.X
//   DEVICE_INFO          → Responde OK:DEVICE_INFO:{nombre},{version},{hw}
//   NAME:{nuevo}         → Renombra a ML-{nuevo}, persiste en NVS
// ====================================================================

#ifndef DEVICE_COMMANDS_H
#define DEVICE_COMMANDS_H

#include <Arduino.h>
#include <Preferences.h>
#include <esp_mac.h>
#include "BluetoothSerial.h"

// Referencia al objeto SerialBT definido en el .ino principal
extern BluetoothSerial SerialBT;

// ====== Versión del firmware ======
// Incrementar en cada release. La app compara con GitHub Releases tag_name.
#define FW_VERSION "1.0.0"

// ====== Descriptor de hardware ======
// Identifica la configuración física del dispositivo
#define DEVICE_HW  "WCMCU8"   // Anillo WCMCU-2812B-8 (8 LEDs WS2812B)

// ====== Nomenclatura ======
// Prefijo inmutable — todos los dispositivos MobileLantern lo usan.
// La app filtra por este prefijo durante el escaneo BT Classic.
#define DEVICE_PREFIX "ML-"

// ====== NVS (Non-Volatile Storage) ======
#define NVS_NAMESPACE "device"
#define NVS_KEY_NAME  "name"
#define DEVICE_NAME_MAX_LEN 20  // Máximo de caracteres para el sufijo

// ====== Estado del módulo ======
Preferences devicePrefs;
String deviceName = "";

// ====== Funciones internas ======

/**
 * Genera el sufijo por defecto a partir de los últimos 2 bytes de la MAC BT.
 * Ejemplo: MAC ...AB:CD → sufijo "ABCD"
 */
String getDefaultSuffix() {
  uint8_t mac[6];
  esp_read_mac(mac, ESP_MAC_BT);
  char suffix[5];
  snprintf(suffix, sizeof(suffix), "%02X%02X", mac[4], mac[5]);
  return String(suffix);
}

// ====== Funciones públicas ======

/**
 * Inicializa el nombre del dispositivo desde NVS o genera uno por defecto.
 * Llamar una vez en setup() ANTES de SerialBT.begin().
 * @return El nombre completo del dispositivo (ej: "ML-A1B2" o "ML-Aula101")
 */
String deviceInit() {
  devicePrefs.begin(NVS_NAMESPACE, true);  // modo solo lectura
  String stored = devicePrefs.getString(NVS_KEY_NAME, "");
  devicePrefs.end();

  if (stored.length() > 0) {
    deviceName = String(DEVICE_PREFIX) + stored;
  } else {
    deviceName = String(DEVICE_PREFIX) + getDefaultSuffix();
  }

  Serial.println("[DEV] Nombre del dispositivo: " + deviceName);
  Serial.println("[DEV] Firmware: v" + String(FW_VERSION));
  Serial.println("[DEV] Hardware: " + String(DEVICE_HW));
  return deviceName;
}

/**
 * Obtiene el nombre actual del dispositivo.
 */
String getDeviceName() {
  return deviceName;
}

/**
 * Obtiene la versión del firmware.
 */
String getFirmwareVersion() {
  return String(FW_VERSION);
}

/**
 * Establece un nuevo sufijo de nombre y lo persiste en NVS.
 * El prefijo ML- es inmutable: NAME:Aula101 → ML-Aula101
 * @param newSuffix Sufijo sin el prefijo ML-
 * @return true si se guardó correctamente
 */
bool setDeviceName(String newSuffix) {
  newSuffix.trim();
  if (newSuffix.length() == 0 || newSuffix.length() > DEVICE_NAME_MAX_LEN) {
    return false;
  }

  devicePrefs.begin(NVS_NAMESPACE, false);  // modo lectura-escritura
  devicePrefs.putString(NVS_KEY_NAME, newSuffix);
  devicePrefs.end();

  deviceName = String(DEVICE_PREFIX) + newSuffix;
  Serial.println("[DEV] ✏️ Nombre cambiado a: " + deviceName);
  return true;
}

// ====== Procesador de comandos ======

/**
 * Procesa comandos VERSION, DEVICE_INFO y NAME:*.
 * @param cmd Comando completo
 * @return true si el comando fue reconocido, false si no
 */
bool processDeviceCommand(String cmd) {

  // === VERSION ===
  if (cmd == "VERSION") {
    String response = "OK:VERSION:" + String(FW_VERSION);
    SerialBT.println(response);
    Serial.println("[DEV] " + response);
    return true;
  }

  // === DEVICE_INFO ===
  if (cmd == "DEVICE_INFO") {
    String response = "OK:DEVICE_INFO:" + deviceName + "," + String(FW_VERSION) + "," + String(DEVICE_HW);
    SerialBT.println(response);
    Serial.println("[DEV] " + response);
    return true;
  }

  // === NAME:{nuevo} ===
  if (cmd.startsWith("NAME:")) {
    String newName = cmd.substring(5);
    if (setDeviceName(newName)) {
      SerialBT.println("OK:NAME:" + deviceName);
      Serial.println("[DEV] ⚠️ Reinicio necesario para que el nombre BT surta efecto");
    } else {
      SerialBT.println("ERROR:INVALID_NAME");
      Serial.println("[DEV] ⚠️ Nombre inválido: " + newName);
    }
    return true;
  }

  // No es un comando de este módulo
  return false;
}

#endif // DEVICE_COMMANDS_H

