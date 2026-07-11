<div align="center">

# 🏮 Mobile Lantern

**MobileLantern: un ecosistema de gestión del uso de la IA generativa y orquestación de asistencia en clase**

Ecosistema compuesto por una aplicación Android y un dispositivo *ambient display* que orquesta la asistencia en actividades colaborativas en el aula. 
Integra IA generativa para ofrecer apoyo contextual al alumnado mientras el docente visualiza en tiempo real el estado de cada grupo mediante señales luminosas y acústicas.

[Sitio web](https://erichgb.github.io/MobileLantern/) · [Manual de usuario](https://erichgb.github.io/MobileLantern/manual/) · [Generador de QR](https://erichgb.github.io/MobileLantern/qr-generator/)

</div>

---

## Estructura

| Carpeta | Descripción |
| --- | --- |
| [`android/`](android/) | App Android — Kotlin · Jetpack Compose · Hilt · Room · Retrofit |
| [`firmware/`](firmware/) | Firmware ESP32 — anillo LED WS2812B, buzzer, Bluetooth SPP, OTA |

## Flujo pedagógico

1. El docente genera códigos QR de sesión desde el [generador web](https://erichgb.github.io/MobileLantern/qr-generator/).
2. El alumnado hace *check-in* escaneando el QR con la app.
3. Se formula una duda y se inicia la fase de resolución colaborativa.
4. Si el grupo necesita apoyo, la app consulta a la IA generativa.
5. El tutor puede intervenir en cualquier momento, guiado por el estado del *display ambiental*.

Cada transición se refleja en la linterna física mediante cambios de color, frecuencia de parpadeo y avisos acústicos.

## Automatización

Los flujos de CI en [`.github/workflows/`](.github/workflows/) publican automáticamente las *releases* de la app Android y el firmware.

---

<div align="center">
<sub>Trabajo Fin de Máster · Universidad Politécnica de Madrid · ETSISI</sub><br>
<sub>Máster Universitario en Software de Sistemas Distribuidos y Empotrados</sub>
</div>
