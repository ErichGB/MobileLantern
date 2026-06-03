// ====================================================================
// ota_commands.h — Módulo OTA (Over-The-Air) para actualización remota
// ====================================================================
// Implementa las 3 vías de actualización descritas en ota.md:
//
//   1. WiFi OTA (ArduinoOTA)  — Desarrollo
//      Comando: OTA_WIFI:SSID,PASS
//      Conecta WiFi → escucha ArduinoOTA → sube desde Arduino IDE
//
//   2. HTTP OTA (GitHub Releases) — Producción ⭐
//      Comando: OTA_HTTP_WIFI:SSID,PASS,URL
//      Conecta WiFi → descarga .bin desde URL → instala → reboot
//
//   3. BT OTA (SPP stream) — Emergencia
//      Comando: OTA_BT
//      App envía binario por SPP: [size 4B][chunks 512B][CRC32 4B]
//
// Comandos adicionales:
//   OTA_STATUS             → Estado actual del proceso OTA
//   OTA_CANCEL             → Cancela OTA en progreso
//
// ⚠️  Durante OTA se deben desactivar efectos LED y buzzer
//     (la señal NeoPixel usa RMT y puede interferir con WiFi timing)
// ====================================================================

#ifndef OTA_COMMANDS_H
#define OTA_COMMANDS_H

#include <Arduino.h>
#include <WiFi.h>
#include <ArduinoOTA.h>
#include <HTTPClient.h>
#include <Update.h>
#include <esp_ota_ops.h>
#include "BluetoothSerial.h"

// Referencia al objeto SerialBT definido en el .ino principal
extern BluetoothSerial SerialBT;

// ====== Configuración de timeouts ======
#define OTA_WIFI_TIMEOUT_MS   15000   // 15s para conectar WiFi
#define OTA_HTTP_TIMEOUT_MS   60000   // 60s para descargar firmware
#define OTA_BT_CHUNK_SIZE     512     // Tamaño de chunk BT OTA (bytes)
#define OTA_BT_TIMEOUT_MS     30000   // 30s timeout entre chunks BT
#define OTA_MAX_FW_SIZE       1900000 // 1.9 MB máximo (esquema particiones OTA)

// ====== Estados OTA ======
// Prefijo ML_ para evitar colisión con ota_state_t de ArduinoOTA.h
// (que define OTA_IDLE, OTA_SUCCESS, etc.)
enum OtaState {
  ML_OTA_IDLE,                // Sin OTA activo
  ML_OTA_WIFI_WAITING,        // ArduinoOTA escuchando en red local
  ML_OTA_HTTP_DOWNLOADING,    // Descargando .bin desde URL
  ML_OTA_BT_RECEIVING,        // Recibiendo binario por SPP
  ML_OTA_INSTALLING,          // Escribiendo en partición OTA
  ML_OTA_SUCCESS,             // Completado, listo para reboot
  ML_OTA_FAILED               // Error durante OTA
};

OtaState otaState = ML_OTA_IDLE;
bool otaWifiMode = false;  // true cuando ArduinoOTA está escuchando

// ====== Estado BT OTA ======
uint32_t otaBtExpectedSize   = 0;
uint32_t otaBtReceived       = 0;
uint32_t otaBtCrc            = 0;
uint32_t otaBtCalculatedCrc  = 0;
uint8_t  otaBtLastPct        = 0;
unsigned long otaBtLastChunkTime = 0;
bool otaBtSizeReceived  = false;
bool otaBtDataComplete   = false;

// ====== Tabla CRC32 (para validación BT OTA) ======
static uint32_t crc32_table[256];
static bool crc32_table_computed = false;

/**
 * Inicializa la tabla de lookup CRC32 (polinomio estándar).
 * Se calcula una sola vez en la primera llamada.
 */
void otaCrc32Init() {
  if (crc32_table_computed) return;
  for (uint32_t i = 0; i < 256; i++) {
    uint32_t crc = i;
    for (int j = 0; j < 8; j++) {
      crc = (crc & 1) ? (crc >> 1) ^ 0xEDB88320 : crc >> 1;
    }
    crc32_table[i] = crc;
  }
  crc32_table_computed = true;
}

/**
 * Actualiza un CRC32 incremental con nuevos datos.
 */
uint32_t otaCrc32Update(uint32_t crc, const uint8_t *data, size_t len) {
  for (size_t i = 0; i < len; i++) {
    crc = crc32_table[(crc ^ data[i]) & 0xFF] ^ (crc >> 8);
  }
  return crc;
}

// ====== Funciones auxiliares ======

/**
 * Conecta a una red WiFi con timeout.
 * @return true si se conectó, false si timeout
 */
bool otaConnectWiFi(String ssid, String password) {
  Serial.printf("[OTA] 📡 Conectando a WiFi: %s\n", ssid.c_str());
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid.c_str(), password.c_str());

  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED) {
    if (millis() - start > OTA_WIFI_TIMEOUT_MS) {
      Serial.println("[OTA] ❌ Timeout WiFi (15s)");
      WiFi.disconnect(true);
      WiFi.mode(WIFI_OFF);
      return false;
    }
    delay(250);
    Serial.print(".");
  }

  Serial.println();
  Serial.println("[OTA] ✅ WiFi conectado: " + WiFi.localIP().toString());
  Serial.printf("[OTA] Heap libre: %u bytes\n", ESP.getFreeHeap());
  return true;
}

/**
 * Desconecta WiFi y libera recursos.
 */
void otaDisconnectWiFi() {
  WiFi.disconnect(true);
  WiFi.mode(WIFI_OFF);
  Serial.println("[OTA] 📡 WiFi desconectado");
}

// ====== Vía 1: WiFi OTA (ArduinoOTA — Desarrollo) ======

/**
 * Conecta WiFi e inicia el servidor ArduinoOTA.
 * El firmware se sube desde Arduino IDE → Herramientas → Puerto de red.
 */
void otaStartWiFi(String ssid, String password) {
  if (!otaConnectWiFi(ssid, password)) {
    SerialBT.println("ERROR:OTA_WIFI:TIMEOUT");
    otaState = ML_OTA_FAILED;
    return;
  }

  ArduinoOTA.setHostname("ESP32-ML-OTA");

  ArduinoOTA.onStart([]() {
    String type = (ArduinoOTA.getCommand() == U_FLASH) ? "firmware" : "filesystem";
    Serial.println("[OTA] 🚀 ArduinoOTA inicio: " + type);
    SerialBT.println("OK:OTA_WIFI:UPDATING");
    otaState = ML_OTA_INSTALLING;
  });

  ArduinoOTA.onEnd([]() {
    Serial.println("\n[OTA] ✅ ArduinoOTA completado");
    SerialBT.println("OK:OTA_WIFI:SUCCESS");
    otaState = ML_OTA_SUCCESS;
  });

  ArduinoOTA.onProgress([](unsigned int progress, unsigned int total) {
    uint8_t pct = (uint8_t)(progress / (total / 100));
    Serial.printf("[OTA] Progreso: %u%%\r", pct);
  });

  ArduinoOTA.onError([](ota_error_t error) {
    Serial.printf("\n[OTA] ❌ Error ArduinoOTA[%u]: ", error);
    switch (error) {
      case OTA_AUTH_ERROR:    Serial.println("Auth Failed");    break;
      case OTA_BEGIN_ERROR:   Serial.println("Begin Failed");   break;
      case OTA_CONNECT_ERROR: Serial.println("Connect Failed"); break;
      case OTA_RECEIVE_ERROR: Serial.println("Receive Failed"); break;
      case OTA_END_ERROR:     Serial.println("End Failed");     break;
    }
    SerialBT.println("ERROR:OTA_WIFI:FAILED");
    otaState = ML_OTA_FAILED;
  });

  ArduinoOTA.begin();
  otaState = ML_OTA_WIFI_WAITING;
  otaWifiMode = true;
  SerialBT.println("OK:OTA_WIFI:READY");
  Serial.println("[OTA] 📡 ArduinoOTA escuchando en " + WiFi.localIP().toString());
}

// ====== Vía 2: HTTP OTA (GitHub Releases — Producción ⭐) ======

/**
 * Conecta WiFi, descarga el .bin desde la URL y lo instala.
 * URL típica: https://github.com/OWNER/REPO/releases/latest/download/firmware.bin
 * Tras instalación exitosa, el ESP32 se reinicia automáticamente.
 */
void otaStartHttp(String ssid, String password, String url) {
  if (!otaConnectWiFi(ssid, password)) {
    SerialBT.println("ERROR:OTA_HTTP:TIMEOUT");
    otaState = ML_OTA_FAILED;
    return;
  }

  SerialBT.println("OK:OTA_HTTP:DOWNLOADING");
  otaState = ML_OTA_HTTP_DOWNLOADING;
  Serial.println("[OTA] 📥 Descargando: " + url);

  HTTPClient http;
  http.setFollowRedirects(HTTPC_STRICT_FOLLOW_REDIRECTS);
  http.setTimeout(OTA_HTTP_TIMEOUT_MS);
  http.begin(url);

  int httpCode = http.GET();

  if (httpCode != HTTP_CODE_OK) {
    Serial.printf("[OTA] ❌ Error HTTP: %d\n", httpCode);
    SerialBT.println("ERROR:OTA_HTTP:HTTP_" + String(httpCode));
    http.end();
    otaDisconnectWiFi();
    otaState = ML_OTA_FAILED;
    return;
  }

  int contentLength = http.getSize();
  if (contentLength <= 0) {
    Serial.println("[OTA] ❌ Tamaño de contenido inválido");
    SerialBT.println("ERROR:OTA_HTTP:INVALID_SIZE");
    http.end();
    otaDisconnectWiFi();
    otaState = ML_OTA_FAILED;
    return;
  }

  Serial.printf("[OTA] 📦 Tamaño firmware: %d bytes\n", contentLength);

  if (contentLength > OTA_MAX_FW_SIZE) {
    Serial.println("[OTA] ❌ Firmware demasiado grande (máx 1.9 MB)");
    SerialBT.println("ERROR:OTA_HTTP:TOO_LARGE");
    http.end();
    otaDisconnectWiFi();
    otaState = ML_OTA_FAILED;
    return;
  }

  if (!Update.begin(contentLength)) {
    Serial.println("[OTA] ❌ Espacio insuficiente para OTA");
    SerialBT.println("ERROR:OTA_HTTP:NO_SPACE");
    http.end();
    otaDisconnectWiFi();
    otaState = ML_OTA_FAILED;
    return;
  }

  otaState = ML_OTA_INSTALLING;
  SerialBT.println("OK:OTA_HTTP:INSTALLING");

  WiFiClient *stream = http.getStreamPtr();
  size_t written = Update.writeStream(*stream);

  if (written != (size_t)contentLength) {
    Serial.printf("[OTA] ❌ Escritura incompleta: %u/%d bytes\n", written, contentLength);
    SerialBT.println("ERROR:OTA_HTTP:WRITE_FAILED");
    Update.abort();
    http.end();
    otaDisconnectWiFi();
    otaState = ML_OTA_FAILED;
    return;
  }

  Serial.printf("[OTA] ✅ Escrito: %u bytes\n", written);

  if (Update.end()) {
    if (Update.isFinished()) {
      Serial.println("[OTA] ✅ HTTP OTA exitoso. Reiniciando...");
      SerialBT.println("OK:OTA_HTTP:SUCCESS");
      otaState = ML_OTA_SUCCESS;
      http.end();
      otaDisconnectWiFi();
      delay(1000);
      ESP.restart();
    } else {
      Serial.println("[OTA] ❌ Update no finalizado");
      SerialBT.println("ERROR:OTA_HTTP:NOT_FINISHED");
      otaState = ML_OTA_FAILED;
    }
  } else {
    Serial.println("[OTA] ❌ Error Update: " + String(Update.getError()));
    SerialBT.println("ERROR:OTA_HTTP:FAILED");
    otaState = ML_OTA_FAILED;
  }

  http.end();
  otaDisconnectWiFi();
}

// ====== Vía 3: BT OTA (SPP stream — Emergencia) ======
// Protocolo binario:
//   1. App envía OTA_BT (texto) → ESP responde OK:OTA_BT:READY
//   2. App envía [4 bytes uint32_t LE] = tamaño del firmware
//   3. App envía chunks de 512 bytes del .bin
//   4. App envía [4 bytes uint32_t LE] = CRC32 del firmware
//   5. ESP valida CRC → escribe OTA → responde OK:OTA_BT:SUCCESS → reboot

/**
 * Resetea todo el estado de BT OTA.
 */
void otaBtReset() {
  otaBtExpectedSize   = 0;
  otaBtReceived       = 0;
  otaBtCrc            = 0;
  otaBtCalculatedCrc  = 0xFFFFFFFF;
  otaBtLastPct        = 0;
  otaBtLastChunkTime  = 0;
  otaBtSizeReceived   = false;
  otaBtDataComplete   = false;
  otaState = ML_OTA_IDLE;
}

/**
 * Inicia el modo BT OTA: prepara CRC32 y espera el tamaño del firmware.
 */
void otaStartBt() {
  otaCrc32Init();
  otaBtReset();
  otaState = ML_OTA_BT_RECEIVING;
  otaBtLastChunkTime = millis();
  SerialBT.println("OK:OTA_BT:READY");
  Serial.println("[OTA] 📲 BT OTA: esperando tamaño del firmware (4 bytes)...");
}

/**
 * Procesa datos binarios entrantes durante BT OTA.
 * Llamar en loop() cuando otaState == ML_OTA_BT_RECEIVING.
 *
 * ⚠️  Cuando esta función retorna true, los datos del socket son binarios
 *     y NO deben parsearse como comandos de texto.
 *
 * @return true si el módulo BT OTA consumió datos o está activo
 */
bool otaBtUpdate() {
  if (otaState != ML_OTA_BT_RECEIVING) return false;

  // --- Timeout: sin datos durante 30s → abortar ---
  if (millis() - otaBtLastChunkTime > OTA_BT_TIMEOUT_MS) {
    Serial.println("[OTA] ❌ BT OTA timeout (30s sin datos)");
    SerialBT.println("ERROR:OTA_BT:TIMEOUT");
    if (otaBtSizeReceived) Update.abort();
    otaBtReset();
    return true;
  }

  if (!SerialBT.available()) return true;  // Activo pero sin datos aún
  otaBtLastChunkTime = millis();

  // --- Paso 1: Leer tamaño del firmware (4 bytes, little-endian) ---
  if (!otaBtSizeReceived) {
    if (SerialBT.available() >= 4) {
      uint8_t sizeBuf[4];
      for (int i = 0; i < 4; i++) sizeBuf[i] = SerialBT.read();
      otaBtExpectedSize = sizeBuf[0]
                        | ((uint32_t)sizeBuf[1] << 8)
                        | ((uint32_t)sizeBuf[2] << 16)
                        | ((uint32_t)sizeBuf[3] << 24);

      Serial.printf("[OTA] 📦 Tamaño esperado: %u bytes\n", otaBtExpectedSize);

      if (otaBtExpectedSize == 0 || otaBtExpectedSize > OTA_MAX_FW_SIZE) {
        Serial.println("[OTA] ❌ Tamaño inválido (0 o > 1.9 MB)");
        SerialBT.println("ERROR:OTA_BT:INVALID_SIZE");
        otaBtReset();
        return true;
      }

      if (!Update.begin(otaBtExpectedSize)) {
        Serial.println("[OTA] ❌ Espacio insuficiente para OTA");
        SerialBT.println("ERROR:OTA_BT:NO_SPACE");
        otaBtReset();
        return true;
      }

      otaBtSizeReceived = true;
      otaBtCalculatedCrc = 0xFFFFFFFF;
      SerialBT.println("OK:OTA_BT:RECEIVING");
      Serial.println("[OTA] 📲 Recibiendo datos del firmware...");
    }
    return true;
  }

  // --- Paso 2: Recibir chunks del firmware ---
  if (!otaBtDataComplete) {
    uint8_t buf[OTA_BT_CHUNK_SIZE];
    size_t remaining = otaBtExpectedSize - otaBtReceived;
    size_t available = (size_t)SerialBT.available();
    size_t toRead = min(available, min((size_t)OTA_BT_CHUNK_SIZE, remaining));

    for (size_t i = 0; i < toRead; i++) {
      buf[i] = SerialBT.read();
    }

    if (toRead > 0) {
      size_t written = Update.write(buf, toRead);
      if (written != toRead) {
        Serial.printf("[OTA] ❌ Error escritura: %u/%u bytes\n", written, toRead);
        SerialBT.println("ERROR:OTA_BT:WRITE_FAILED");
        Update.abort();
        otaBtReset();
        return true;
      }

      otaBtCalculatedCrc = otaCrc32Update(otaBtCalculatedCrc, buf, toRead);
      otaBtReceived += toRead;

      // Reportar progreso cada 10%
      uint8_t pct = (uint8_t)((otaBtReceived * 100UL) / otaBtExpectedSize);
      if (pct / 10 > otaBtLastPct / 10) {
        Serial.printf("[OTA] Progreso: %u%% (%u/%u bytes)\n", pct, otaBtReceived, otaBtExpectedSize);
        SerialBT.println("OK:OTA_BT:PROGRESS:" + String(pct));
        otaBtLastPct = pct;
      }
    }

    // ¿Recibimos todos los bytes del firmware?
    if (otaBtReceived >= otaBtExpectedSize) {
      otaBtDataComplete = true;
      otaBtCalculatedCrc ^= 0xFFFFFFFF;  // Finalizar CRC32
      Serial.printf("[OTA] 📦 Datos completos (%u bytes). Esperando CRC32...\n", otaBtReceived);
    }
    return true;
  }

  // --- Paso 3: Leer CRC32 (4 bytes, little-endian) y validar ---
  if (SerialBT.available() >= 4) {
    uint8_t crcBuf[4];
    for (int i = 0; i < 4; i++) crcBuf[i] = SerialBT.read();
    otaBtCrc = crcBuf[0]
             | ((uint32_t)crcBuf[1] << 8)
             | ((uint32_t)crcBuf[2] << 16)
             | ((uint32_t)crcBuf[3] << 24);

    Serial.printf("[OTA] CRC32 recibido: 0x%08X, calculado: 0x%08X\n",
                  otaBtCrc, otaBtCalculatedCrc);

    if (otaBtCrc != otaBtCalculatedCrc) {
      Serial.println("[OTA] ❌ CRC32 no coincide!");
      SerialBT.println("ERROR:OTA_BT:CRC_MISMATCH");
      Update.abort();
      otaBtReset();
      return true;
    }

    // CRC válido → finalizar Update
    if (Update.end()) {
      if (Update.isFinished()) {
        Serial.println("[OTA] ✅ BT OTA exitoso. Reiniciando...");
        SerialBT.println("OK:OTA_BT:SUCCESS");
        otaState = ML_OTA_SUCCESS;
        delay(1000);
        ESP.restart();
      } else {
        Serial.println("[OTA] ❌ Update no finalizado");
        SerialBT.println("ERROR:OTA_BT:NOT_FINISHED");
        otaBtReset();
      }
    } else {
      Serial.println("[OTA] ❌ Error Update: " + String(Update.getError()));
      SerialBT.println("ERROR:OTA_BT:FAILED");
      otaBtReset();
    }
    return true;
  }

  return true;  // Activo, esperando más datos
}

// ====== Funciones de ciclo de vida ======

/**
 * Inicializa el módulo OTA. Llamar una vez en setup().
 * Confirma que el firmware actual es válido (protección rollback).
 */
void otaInit() {
  esp_ota_mark_app_valid_cancel_rollback();
  Serial.println("[OTA] Firmware marcado como válido (rollback cancelado)");
  Serial.println("[OTA] Módulo OTA inicializado");
}

/**
 * Actualiza el estado OTA. Llamar en cada iteración de loop().
 * Gestiona el polling de ArduinoOTA cuando WiFi OTA está activo.
 */
void otaLoop() {
  // Polling ArduinoOTA cuando está en modo WiFi
  if (otaWifiMode && otaState == ML_OTA_WIFI_WAITING) {
    ArduinoOTA.handle();
  }

  // Reboot pendiente tras OTA exitoso
  if (otaState == ML_OTA_SUCCESS) {
    delay(1000);
    ESP.restart();
  }
}

/**
 * Cancela cualquier OTA en progreso. Libera WiFi y/o aborta Update.
 */
void otaCancel() {
  if (otaWifiMode) {
    otaDisconnectWiFi();
    otaWifiMode = false;
  }
  if (otaState == ML_OTA_BT_RECEIVING && otaBtSizeReceived) {
    Update.abort();
  }
  otaBtReset();
  otaState = ML_OTA_IDLE;
  Serial.println("[OTA] ⏹️ OTA cancelado");
}

/**
 * @return true si hay un proceso OTA activo (no IDLE ni FAILED)
 */
bool isOtaActive() {
  return otaState != ML_OTA_IDLE && otaState != ML_OTA_FAILED;
}

// ====== Procesador de comandos OTA ======

/**
 * Procesa comandos OTA_WIFI:, OTA_HTTP_WIFI:, OTA_BT, OTA_STATUS, OTA_CANCEL.
 * @param cmd Comando completo
 * @return true si el comando fue reconocido como OTA, false si no
 */
bool processOtaCommand(String cmd) {

  // === OTA_STATUS ===
  if (cmd == "OTA_STATUS") {
    String statusStr;
    switch (otaState) {
      case ML_OTA_IDLE:             statusStr = "IDLE";             break;
      case ML_OTA_WIFI_WAITING:     statusStr = "WIFI_WAITING";     break;
      case ML_OTA_HTTP_DOWNLOADING: statusStr = "HTTP_DOWNLOADING"; break;
      case ML_OTA_BT_RECEIVING: {
        uint8_t pct = (otaBtExpectedSize > 0)
          ? (uint8_t)((otaBtReceived * 100UL) / otaBtExpectedSize)
          : 0;
        statusStr = "BT_RECEIVING:" + String(pct) + "%";
        break;
      }
      case ML_OTA_INSTALLING:       statusStr = "INSTALLING";       break;
      case ML_OTA_SUCCESS:          statusStr = "SUCCESS";          break;
      case ML_OTA_FAILED:           statusStr = "FAILED";           break;
    }
    SerialBT.println("OK:OTA_STATUS:" + statusStr);
    Serial.println("[OTA] Estado: " + statusStr);
    return true;
  }

  // === OTA_CANCEL ===
  if (cmd == "OTA_CANCEL") {
    otaCancel();
    SerialBT.println("OK:OTA_CANCEL");
    return true;
  }

  // Bloquear nuevos OTA si ya hay uno en progreso
  if (isOtaActive()) {
    if (cmd.startsWith("OTA_WIFI:") || cmd.startsWith("OTA_HTTP_WIFI:") || cmd == "OTA_BT") {
      SerialBT.println("ERROR:OTA_IN_PROGRESS");
      Serial.println("[OTA] ⚠️ OTA ya en progreso, comando rechazado");
      return true;
    }
  }

  // === OTA_WIFI:SSID,PASS ===
  if (cmd.startsWith("OTA_WIFI:")) {
    String params = cmd.substring(9);
    int commaIdx = params.indexOf(',');
    if (commaIdx < 0) {
      SerialBT.println("ERROR:OTA_WIFI:INVALID_PARAMS");
      Serial.println("[OTA] ⚠️ Formato inválido (usar OTA_WIFI:SSID,PASS)");
      return true;
    }
    String ssid = params.substring(0, commaIdx);
    String pass = params.substring(commaIdx + 1);
    LedCmd lcmd = {}; lcmd.type = LED_CMD_STOP;
    xQueueSend(ledQueue, &lcmd, 0);
    BuzzerCmd bcmd = {}; bcmd.type = BUZZER_CMD_STOP;
    xQueueSend(buzzerQueue, &bcmd, 0);
    Serial.println("[OTA] ═══════════════════════════════════════════════");
    Serial.println("[OTA] Iniciando WiFi OTA (ArduinoOTA)...");
    Serial.println("[OTA] ═══════════════════════════════════════════════");
    otaStartWiFi(ssid, pass);
    return true;
  }

  // === OTA_HTTP_WIFI:SSID,PASS,URL ===
  if (cmd.startsWith("OTA_HTTP_WIFI:")) {
    String params = cmd.substring(14);
    int comma1 = params.indexOf(',');
    int comma2 = params.indexOf(',', comma1 + 1);
    if (comma1 < 0 || comma2 < 0) {
      SerialBT.println("ERROR:OTA_HTTP:INVALID_PARAMS");
      Serial.println("[OTA] ⚠️ Formato inválido (usar OTA_HTTP_WIFI:SSID,PASS,URL)");
      return true;
    }
    String ssid = params.substring(0, comma1);
    String pass = params.substring(comma1 + 1, comma2);
    String url  = params.substring(comma2 + 1);

    // Validar HTTPS
    if (!url.startsWith("https://")) {
      SerialBT.println("ERROR:OTA_HTTP:HTTPS_REQUIRED");
      Serial.println("[OTA] ⚠️ Solo se permiten URLs HTTPS");
      return true;
    }

    LedCmd lcmd = {}; lcmd.type = LED_CMD_STOP;
    xQueueSend(ledQueue, &lcmd, 0);
    BuzzerCmd bcmd = {}; bcmd.type = BUZZER_CMD_STOP;
    xQueueSend(buzzerQueue, &bcmd, 0);
    Serial.println("[OTA] ═══════════════════════════════════════════════");
    Serial.println("[OTA] Iniciando HTTP OTA (GitHub Releases)...");
    Serial.println("[OTA] URL: " + url);
    Serial.println("[OTA] ═══════════════════════════════════════════════");
    otaStartHttp(ssid, pass, url);
    return true;
  }

  // === OTA_BT ===
  if (cmd == "OTA_BT") {
    LedCmd lcmd = {}; lcmd.type = LED_CMD_STOP;
    xQueueSend(ledQueue, &lcmd, 0);
    BuzzerCmd bcmd = {}; bcmd.type = BUZZER_CMD_STOP;
    xQueueSend(buzzerQueue, &bcmd, 0);
    Serial.println("[OTA] ═══════════════════════════════════════════════");
    Serial.println("[OTA] Iniciando BT OTA (emergencia)...");
    Serial.println("[OTA] Protocolo: [size 4B] [chunks 512B] [CRC32 4B]");
    Serial.println("[OTA] ═══════════════════════════════════════════════");
    otaStartBt();
    return true;
  }

  // No es un comando OTA
  return false;
}

#endif // OTA_COMMANDS_H

