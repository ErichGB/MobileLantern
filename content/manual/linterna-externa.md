---
title: "Linterna Externa Bluetooth"
date: 2025-01-25T12:00:00Z
draft: false
weight: 5
description: "Cómo emparejar, conectar y reconectar la linterna externa Bluetooth de MobileLantern"
---

# 📡 Linterna Externa Bluetooth

Si elige el modo **linterna externa** en la pantalla de bienvenida, la app se comunica por Bluetooth con un dispositivo equipado con LED RGB y zumbador. Esta guía describe cómo emparejar y conectar la linterna por primera vez, así como las opciones de reconexión durante una sesión.

---

## Métodos de Conexión

Existen dos formas de vincular su linterna externa con la aplicación:

| Método | Descripción | Cuándo usarlo |
|--------|-------------|---------------|
| 📡 **Escanear QR** | Escanee el código QR adhesivo de la linterna | Primera vez o emparejamiento rápido sin buscar |
| 🔍 **Buscar** | Pulse "Buscar" para listar los dispositivos cercanos | Si ya emparejó antes o no dispone del QR |

---

## Emparejamiento mediante QR del Dispositivo

Cada linterna externa lleva un código QR adhesivo con su identificador único. Este es el método más rápido:

1. **Encienda la linterna externa.** El anillo LED mostrará un parpadeo azul tenue indicando que está lista para conectar.
2. En la pantalla de linterna externa, pulse el botón **"Escanear QR"**.
3. Apunte la cámara al código QR pegado en el dispositivo.
4. La app lee automáticamente el enlace y comienza el emparejamiento.
5. Si es la primera vez, aparecerá el **diálogo de emparejamiento del sistema**: confirme pulsando **"Vincular"**.
6. Tras emparejar, la app establece la conexión Bluetooth.
7. La linterna emite un **pitido de bienvenida** y enciende brevemente el LED como confirmación.

> **💡 Consejo:** El QR de conexión es diferente al QR de acceso a sesión. Identifíquelo porque suele estar **adherido físicamente a la linterna** y es más pequeño que el QR de clase.

---

## Emparejamiento mediante Búsqueda

Si no dispone del QR adhesivo o la linterna ya fue emparejada anteriormente:

1. **Encienda la linterna externa** y espere al parpadeo azul.
2. En la pantalla de linterna externa, pulse el botón **"Buscar"**.
3. Aparecerá un panel con los dispositivos Bluetooth disponibles. La app **filtra automáticamente** mostrando solo los que tienen el prefijo `ML-` (por ejemplo, `ML-5DAA`).
4. Seleccione su linterna de la lista.
5. Si nunca se emparejó, el sistema le pedirá la confirmación de vinculación (**"Vincular"**).
6. La conexión se establece y la linterna confirma con un pitido y un destello del LED.

> **⚠️ Importante:** Tanto el **Bluetooth** como la **Ubicación** deben estar activados en su dispositivo. Android requiere el permiso de ubicación para descubrir dispositivos Bluetooth cercanos. Si alguno está desactivado, la app mostrará un diálogo indicándole cómo habilitarlos.

---

## Estados durante la Conexión

La aplicación muestra en todo momento el estado del proceso de emparejamiento:

| Estado | Descripción | Acción del usuario |
|--------|-------------|--------------------|
| 🔍 **Buscando** | Escaneando dispositivos con prefijo `ML-` | Espere unos segundos |
| 📡 **Emparejando** | Estableciendo vínculo con el dispositivo seleccionado | Confirme en el diálogo del sistema |
| ⚡ **Conectando** | Estableciendo comunicación con la linterna | Espere unos instantes |
| ✅ **Conectado** | Conexión completada, pitido de confirmación | La pantalla se cierra automáticamente |
| ❌ **Error** | No se pudo completar la conexión | Reintente con "Buscar" o "Escanear QR" |

> **📝 Nota:** Si la conexión se establece correctamente, la app cierra el panel de búsqueda automáticamente y regresa a la pantalla principal. No es necesario pulsar ningún botón adicional.

---

## Reconexión durante una Sesión

Si la conexión Bluetooth se pierde durante una sesión activa (por ejemplo, si la linterna se apaga o se aleja demasiado):

1. Aparecerá un **aviso en la barra superior** indicando la desconexión.
2. Tendrá dos opciones:
   - **"Reconectar linterna"**: Reabre la pantalla de emparejamiento **sin perder la sesión** activa.
   - **"Cambiar a modo integrado"**: Conmuta al flash del teléfono para continuar sin la linterna externa.
3. También puede reconectar desde el **menú lateral (drawer)**, donde encontrará el enlace **"Reconectar linterna"**.

> **💡 Consejo:** Desde el menú **⋮** (tres puntos) de la pantalla de linterna externa puede acceder al **Historial de conexiones**, que muestra los últimos eventos de emparejamiento y conexión con su dispositivo.

---

## Ventajas frente a la linterna integrada

- LED **RGB real** (verde y rojo nativos, no monocromo).
- No sobrecalienta el teléfono ni consume su batería.
- Permite **color + parpadeo + sonido** coordinados.
- No se muestran diálogos de "Coloca tu filtro" (cambia de color automáticamente).

---

## Próximos pasos

- Vuelve a la [guía de uso básico]({{< ref "uso-basico" >}}) para continuar con la sesión
- Consulta las [preguntas frecuentes]({{< ref "faq" >}}) si tienes dudas
