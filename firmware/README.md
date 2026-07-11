# 🔧 Firmware — MobileLantern ESP32

Firmware para el módulo **ESP32-D0WD-V3** que actúa como *display ambiental* del ecosistema MobileLantern. Controla un anillo LED WS2812B y un buzzer pasivo, comunicándose con la app Android vía Bluetooth Classic (SPP).

---

## Hardware

| Componente | Modelo | Pin |
|---|---|---|
| Microcontrolador | ESP32 Dev Module (Dual Core, 240 MHz) | — |
| Anillo LED | WCMCU-2812B-8 (8× WS2812B) | GPIO 13 (DIN) |
| Buzzer pasivo | Módulo S/V/G | GPIO 27 (S) |

---

## Arquitectura

```
MobileLantern-Android.ino    ← Entry point: setup(), loop(), BT callback
├── freertos_tasks.h         ← Colas, mutex y tipos de comando (FreeRTOS)
├── led_commands.h           ← Control del anillo: color, brillo, efectos
├── buzzer_commands.h        ← Tonos, beeps y melodías
├── device_commands.h        ← Identidad (ML-XXXX), NVS, versión
└── ota_commands.h           ← Actualización remota (WiFi / HTTP / BT)
```

Las tareas LED y buzzer corren en **Core 1** como tareas FreeRTOS independientes, comunicadas por colas (`ledQueue`, `buzzerQueue`).

---

## Protocolo de comandos (Bluetooth SPP)

| Comando | Respuesta | Descripción |
|---|---|---|
| `LED_ON` / `LED_OFF` | — | Encender / apagar anillo |
| `COLOR:R,G,B` | — | Establecer color (0-255 por canal) |
| `BRIGHTNESS:N` | — | Ajustar brillo global (0-255) |
| `EFFECT:RAINBOW\|PULSE\|SPIN\|BREATHE\|NONE` | — | Activar/desactivar efecto |
| `BUZZER:ON` / `BUZZER:OFF` | — | Tono continuo / silencio |
| `BUZZER:BEEP` / `BUZZER:BEEP:N` | — | 1 o N pitidos (máx. 10) |
| `BUZZER:TONE:WELCOME\|GOODBYE\|NOTIFY\|SUCCESS\|ALERT` | — | Melodía predefinida |
| `VERSION` | `OK:VERSION:X.X.X` | Versión del firmware |
| `DEVICE_INFO` | `OK:DEVICE_INFO:{name},{ver},{hw}` | Metadatos completos |
| `NAME:{nuevo}` | `OK:NAME:ML-{nuevo}` | Renombrar dispositivo (persiste en NVS) |
| `OTA_WIFI:SSID,PASS` | `OK:OTA_WIFI:...` | OTA por ArduinoOTA (desarrollo) |
| `OTA_HTTP_WIFI:SSID,PASS,URL` | `OK:OTA_HTTP:...` | OTA por HTTP (producción) |
| `OTA_BT` | `OK:OTA_BT:READY` | OTA por Bluetooth SPP (emergencia) |
| `OTA_STATUS` / `OTA_CANCEL` | — | Consultar / cancelar OTA activo |

---

## Configuración del IDE

> ⚠️ **Obligatorio**: el firmware (~1.8 MB) no cabe con el esquema de particiones por defecto.

| Parámetro | Valor |
|---|---|
| Board | ESP32 Dev Module |
| Partition Scheme | **Minimal SPIFFS (1.9 MB APP with OTA / 190 KB SPIFFS)** |
| Upload Speed | 921600 |
| Flash Size | 4 MB (32 Mb) |
| Crystal | 40 MHz |

**Dependencias** (instalar desde el Library Manager de Arduino IDE):

- `Adafruit NeoPixel`
- `BluetoothSerial` (incluida en ESP32 Arduino Core)

---

## Comportamiento por defecto

| Estado | LED | Buzzer |
|---|---|---|
| Sin conexión BT | Parpadeo azul suave | — |
| Cliente conectado | Apagado (espera comandos) | — |
| Cliente desconectado | Vuelve a parpadeo | Se detiene cualquier tono activo |

---

## Deep Link

Al iniciar, el dispositivo imprime por Serial un deep link para vincular rápidamente con la app:

```
mobilelantern://connect?device=ML-XXXX&mac=XX:XX:XX:XX:XX:XX
```

---

*Firmware v1.0.0 · ESP32 Arduino Core*

