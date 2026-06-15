---
title: "Uso Básico"
date: 2025-01-25T12:00:00Z
draft: false
weight: 4
description: "Guía completa paso a paso sobre cómo usar MobileLantern en clase"
---

# 📖 Guía de Uso Paso a Paso

Esta guía te enseñará cómo usar MobileLantern durante tus sesiones de clase, desde la pantalla de bienvenida hasta la resolución de dudas.

---

## 1. Pantalla de Bienvenida

Al abrir la aplicación, verá la pantalla de bienvenida con una breve descripción del sistema.

**Elementos de la pantalla:**

- **Descripción del sistema**: Texto explicativo sobre la aplicación.
- **Selector de linterna**: Dos tarjetas para elegir el tipo de señalización:
  - 🔦 **Linterna integrada** (flash del teléfono).
  - 📡 **Linterna externa** (Bluetooth, LED RGB).

  Cada tarjeta incluye una flecha para **probar** la linterna seleccionada.
- **Botón principal** con etiqueta dinámica:
  - **"Empezar"**: Sin sesión activa → abre el panel de escaneo QR.
  - **"Continuar"**: Con sesión válida → pasa a la pantalla de preguntas.
  - **Deshabilitado**: Mientras se valida la sesión o si no se ha seleccionado una linterna.
- **Aviso "Sesión expirada"**: Aparece si su sesión anterior ya no es válida.
- **Logos institucionales**: En la parte inferior se muestran los logos de las universidades colaboradoras (UPM y UPF).

**Pasos recomendados al iniciar:**

1. **Seleccione una linterna** (integrada o externa) tocando una tarjeta.
2. **Pruebe la linterna** con la flecha de la tarjeta.
3. **Pulse el botón principal** (**"Empezar"** o **"Continuar"**).

> **💡 Consejo:** El botón principal no se activa hasta que haya elegido una linterna.

> **📡 Linterna externa:** Si va a usar una linterna externa Bluetooth, consulte la [guía de emparejamiento]({{< ref "linterna-externa" >}}).

---

## 1.1. Advertencia de Linterna del Sistema

Si la linterna de su dispositivo está encendida por otra aplicación (como la linterna del sistema o una app de terceros), MobileLantern mostrará una advertencia bloqueante.

**¿Qué significa esta advertencia?**

La aplicación necesita control exclusivo de la linterna para funcionar correctamente. Si detecta que la linterna ya está encendida externamente, mostrará el mensaje:

> "La linterna está encendida. Por favor, apágala para un correcto funcionamiento de la aplicación."

**¿Qué hacer?**

1. Apague la linterna desde el panel de notificaciones o la aplicación que la esté usando
2. La advertencia desaparecerá automáticamente cuando la linterna esté apagada
3. Continúe con el uso normal de MobileLantern

> **⚠️ Importante:** Esta advertencia solo aplica cuando se ha seleccionado la **linterna integrada**. Con la linterna externa Bluetooth no hay conflicto con el flash del sistema.

---

## 2. Check-In (Escaneo de Código QR)

El check-in se realiza mediante un **panel emergente** que se abre al pulsar **"Empezar"** en la pantalla de bienvenida.

**Pasos:**
1. Conceda el permiso de cámara la primera vez
2. Apunte la cámara al código QR proporcionado por el profesor
3. La aplicación escaneará y procesará automáticamente el código
4. Al autenticarse, el panel se cierra y pasa a la pantalla de preguntas

> **💡 Consejo:** Mantenga el código QR dentro del marco de la cámara y asegúrese de tener buena iluminación.

---

## 3. Registrar una Pregunta

Una vez autenticado, llegará a la pantalla principal donde puede registrar su duda o pregunta.

**Pasos:**
1. Escriba su pregunta o duda en el campo de texto
2. Sea específico y claro en su pregunta
3. Pulse el botón **"Registrar"**
4. Si usa **linterna integrada**, aparecerá el diálogo indicándole que coloque el **filtro verde**. Con **linterna externa** el LED cambia a verde automáticamente y no se muestra el diálogo.

**¿Qué sucede ahora?**
- 🔦 La **linterna** se enciende en verde (constante)
- ⏱️ El **temporizador** comienza a contar
- 📍 Su profesor puede ver que está trabajando en una pregunta

> **📝 Nota:** Si sale de la pantalla con texto sin registrar, aparecerá un diálogo "¿Perder los cambios?" para confirmar.

---

## 4. Fase Colaborativa (Filtro Verde)

Durante esta fase, trabaje con sus compañeros de grupo para resolver la pregunta.

**Elementos de la pantalla:**
- **Pregunta original** (expandible para ver completa)
- **Campo de respuesta**: Escriba aquí la solución encontrada
- **Switch de evaluación**: Un interruptor que pregunta "¿Crees que la respuesta colaborativa es suficientemente buena?"
  - ✅ **"Sí"**: Si resolvió la pregunta exitosamente de forma colaborativa
  - ❌ **"No"**: Si no pudo resolver la pregunta y necesita más ayuda
- **Botón "Registrar"**: Confirma su respuesta y evaluación

### Opción A: Respuesta Correcta ✅

Si **logró resolver** la pregunta colaborativamente:

1. Escriba la respuesta en el campo correspondiente
2. Active el switch a **"Sí"** para indicar que la respuesta colaborativa es suficientemente buena
3. Pulse el botón **"Registrar"**
4. **Aparecerá un diálogo de felicitación** 🎉
5. La linterna se **apagará**
6. Regresará a la pantalla de registrar nueva pregunta

### Opción B: Respuesta Incorrecta ❌

Si **no logró resolver** la pregunta:

1. Escriba el intento de respuesta
2. Active el switch a **"No"** para indicar que la respuesta colaborativa no es suficientemente buena
3. Pulse el botón **"Registrar"**
4. Si usa **linterna integrada**, aparecerá el diálogo para colocar el **filtro rojo**. Con **linterna externa** el LED cambia a rojo automáticamente sin diálogo.

**¿Qué cambia?**
- 🔴 Cambie el filtro verde por el **filtro rojo** (solo linterna integrada)
- 🔦 La linterna comienza a **parpadear** tras unos minutos
- 🤖 Se activa automáticamente la **asistencia por IA**

---

## 5. Asistencia por IA (Filtro Rojo)

Cuando entra en la fase de asistencia por IA, la aplicación consulta automáticamente al asistente virtual.

**Elementos de la pantalla:**

La pantalla muestra tres tarjetas principales que representan el flujo completo de la consulta:

- **👋 Pregunta**: Muestra la pregunta original que registró anteriormente
- **💡 Respuesta colaborativa**: Muestra la respuesta que intentó con sus compañeros antes de recurrir a la IA
- **🤖 Respuesta de IA**: Tarjeta que muestra la respuesta completa y detallada de la IA. La IA analiza tanto su pregunta como la respuesta colaborativa para proporcionar una explicación contextualizada y guiada que le ayude a comprender mejor el tema

**Elementos interactivos:**

- **Feedback de resolución**: Después de la respuesta de la IA, encontrará la pregunta "¿Has resuelto tu duda con la IA?" con dos opciones:
  - **Botón "Sí"** (azul): Indica que la respuesta de la IA resolvió su duda satisfactoriamente
  - **Botón "No"** (rojo): Indica que aún necesita más ayuda o que la respuesta no fue suficiente
- 🔇 **Botón de silencio**: Icono discreto en la parte inferior de la pantalla que permite silenciar las alertas acústicas en caso de emergencia
- 👨‍🏫 **Botón flotante**: Botón circular en la esquina inferior derecha que solicita intervención directa del profesor cuando necesita ayuda presencial

### Interacción Continua con la IA

Si necesita más clarificaciones después de recibir la respuesta de la IA:

1. Pulse el botón **"No"** (rojo) cuando la aplicación pregunte si ha resuelto su duda
2. Aparecerá una tarjeta con el título **"¿Nueva pregunta a la IA?"**
3. Escriba su pregunta adicional en el campo de texto
4. Pulse el botón **"Enviar"**
5. La IA responderá a su nueva pregunta y podrá continuar la conversación

### Finalizar con la IA

Cuando la IA haya proporcionado su respuesta, la aplicación le preguntará si ha resuelto su duda:

1. Revise la respuesta de la IA cuidadosamente
2. Si la respuesta resolvió su duda, pulse el botón **"Sí"** (azul)
3. Si aún necesita más ayuda, pulse el botón **"No"** (rojo) para continuar con la interacción o solicitar ayuda del profesor
4. Al seleccionar **"Sí"**, **aparecerá un diálogo de felicitación** 🤖✨
5. La linterna se **apagará**
6. Regresará a la pantalla de registrar nueva pregunta

> **📝 Nota:** Si el profesor ha intervenido y resuelto finalmente su pregunta, puede pulsar el **botón flotante** 👨‍🏫 en la esquina inferior derecha para registrar su solución.

**Mensaje de felicitación:**
> "🤖✨ ¡Excelente! Has resuelto tu duda con la ayuda de la IA. ¡Sigue aprendiendo!"

---

## 6. Registrar Intervención del Profesor

En cualquier momento durante la fase de IA, puede solicitar ayuda directa del profesor.

**Pasos:**
1. Espere a que su profesor se acerque a su grupo
2. Pulse el **botón flotante azul** con icono de persona
3. Ingrese una descripción de la intervención del profesor
4. Se registra la intervención
5. **Aparecerá un diálogo de confirmación** 👨‍🏫✅
6. La linterna se **apaga**

**Mensaje de confirmación:**
> "👨‍🏫✅ ¡Problema resuelto! El profesor ha intervenido y resuelto tu duda. ¡Continúa con tu aprendizaje!"

> **⚠️ Importante:** Use este botón cuando realmente necesite ayuda presencial del profesor.

---

## 7. Botón de Silencio de Emergencia

En la pantalla de asistencia por IA, encontrará un botón discreto para silenciar las alertas acústicas en caso de emergencia.

**Ubicación:** Esquina de la pantalla, icono semitransparente 🔊

**Cómo activarlo:**

El botón requiere **3 toques consecutivos** para activarse (esto evita activaciones accidentales):

1. **Primer toque**: El icono crece ligeramente
2. **Segundo toque**: El icono crece un poco más
3. **Tercer toque**: Se activa el silencio y el icono cambia a 🔇

> **⏱️ Tiempo límite:** Si pasan más de 2 segundos entre toques, el contador se reinicia.

**Estados del botón:**

| Icono | Estado | Significado |
|-------|--------|-------------|
| 🔊 | Normal | Las alertas acústicas están activas |
| 🔇 | Silenciado | Las alertas acústicas están desactivadas |

**¿Cuándo usarlo?**

Este botón está diseñado para **escenarios de emergencia** donde muchos estudiantes están simultáneamente en la fase de IA y las alertas acústicas múltiples pueden crear un ambiente disruptivo en el aula.

> **⚠️ Importante:** 
> - **No use este botón a menos de ser realmente necesario.** Las alertas acústicas son una señal importante para el profesor.
> - El silencio afecta solo a las alertas acústicas. La linterna y las señales visuales continuarán funcionando normalmente.
> - Use solo en situaciones donde múltiples alertas simultáneas estén causando problemas en el aula.

---

## 8. Historial y Búsqueda

### Menú Lateral (Drawer)

Acceda al historial completo de su grupo desde cualquier pantalla.

> **📝 Nota sobre Modo Aula:** Si su código QR incluye un identificador de aula, el historial mostrará las preguntas de **todos los grupos del aula**, no solo las de su grupo. Esto permite a los profesores tener una vista completa de las dudas de toda la clase.

**Cómo abrir el menú:**
- Pulse el **icono de menú** en la esquina superior derecha

**Elementos de la pantalla:**

- 🔍 **Barra de búsqueda**: Encuentre preguntas escribiendo palabras clave.
- ➕ **Botón Nueva Pregunta**: Botón azul con icono "+" para iniciar una nueva sesión.
- 🔄 **Deslizar hacia abajo**: Refresca la lista desde el servidor.
- 📅 **Organización temporal**: Las preguntas se agrupan por "Hoy", "Ayer", "Esta semana", "Semana pasada", "Este mes", "Mes pasado" y luego por mes/año.
- 📝 **Tarjetas de preguntas**: Cada pregunta muestra:
  - Icono según el estado
  - Texto de la pregunta (primeras líneas)
  - Estado: Completada, Consulta IA, Respuesta o Pendiente
  - Fecha de registro
- 📡 **Reconectar linterna**: Si usa linterna externa Bluetooth, un enlace permite reabrir la pantalla de emparejamiento.
- 👤 **Perfil de usuario**: En la parte inferior muestra su nombre, email y el total de preguntas (resueltas y pendientes). Incluye botón "Cerrar sesión".

**Ver detalles:**
Toque cualquier pregunta del historial para ver la pregunta completa, respuestas colaborativas, respuestas de la IA, intervención del profesor y la línea temporal completa.

---

## 🎓 Mejores Prácticas

### Para Estudiantes

✅ **Elija y pruebe su linterna** (integrada o externa Bluetooth) en la pantalla de bienvenida antes de comenzar  
✅ **Asegúrese de que ninguna otra app** esté usando la linterna integrada  
✅ **Intente resolver colaborativamente primero** antes de recurrir a la IA  
✅ **Sea específico** en sus preguntas tanto a compañeros como a la IA  
✅ **Documente su proceso** escribiendo las respuestas intentadas  
✅ **Use la IA para aprender**, no solo para obtener respuestas  
✅ **Solicite al profesor** cuando realmente esté bloqueado  
✅ **Revise el historial** antes de hacer preguntas repetidas  
✅ **Use el botón de silencio** solo en emergencias (requiere 3 toques)

### Para Profesores

✅ **Monitoree las luces** de los dispositivos periódicamente  
✅ **Priorice los filtros rojos** con parpadeo activo (>3 min en fase IA)  
✅ **Intervenga estratégicamente** cuando suenen las alertas (>6 min)  
✅ **Revise el historial** después de clase para identificar patrones  
✅ **Ajuste el contexto pedagógico** según las necesidades del curso  
✅ **Fomente el uso colaborativo** antes de recurrir a la IA  
✅ **Use el Modo Aula** para ver todas las preguntas de la clase  
✅ **Genere códigos QR con anticipación** y asegúrese de que sean legibles

---

## Próximos pasos

- Configura la [linterna externa Bluetooth]({{< ref "linterna-externa" >}}) si dispones de una
- Consulta las [preguntas frecuentes]({{< ref "faq" >}}) para dudas comunes
- Revisa [qué es MobileLantern]({{< ref "que-es" >}}) para entender mejor el sistema
