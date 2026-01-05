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

Al abrir la aplicación, verá la pantalla de bienvenida con el logotipo de MobileLantern y una descripción del sistema.

**Elementos de la pantalla:**

- **Descripción del sistema**: Texto explicativo sobre la aplicación y su funcionamiento con linterna, vasos de colores y alertas acústicas
- **Botón "Encender/Apagar linterna"**: Permite probar que la linterna funciona correctamente antes de comenzar la sesión
- **Botón "Empezar"**: Inicia el proceso de check-in escaneando un código QR
- **Botón "Continuar"** (solo visible si hay sesión activa): Permite retomar una sesión anterior sin necesidad de escanear el QR nuevamente
- **Logos institucionales**: En la parte inferior se muestran los logos de las universidades colaboradoras (UPM y UPF)

**Acciones disponibles:**

1. **Probar la linterna**: Pulse el botón **"Encender linterna"** para verificar que la linterna de su dispositivo funciona correctamente. Esto es recomendable antes de cada sesión.

2. **Iniciar nueva sesión**: Pulse **"Empezar"** para ir a la pantalla de escaneo de código QR.

3. **Continuar sesión existente**: Si ya tiene una sesión activa de una clase anterior, aparecerá el botón **"Continuar"** que le llevará directamente a la pantalla de preguntas sin necesidad de escanear nuevamente el QR.

> **💡 Consejo:** Pruebe siempre la linterna antes de comenzar para asegurarse de que su dispositivo está listo para la sesión.

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

> **⚠️ Importante:** Esta advertencia aparecerá también en la pantalla de registro de preguntas si la linterna se enciende externamente durante la sesión.

---

## 2. Escanear Código QR (Check-In)

La aplicación le pedirá acceso a la cámara. Esto es necesario para escanear el código QR.

**Pasos:**
1. Conceda el permiso de cámara
2. Apunte la cámara al código QR proporcionado por el profesor
3. La aplicación escaneará automáticamente el código
4. Espere la confirmación de inicio de sesión

> **💡 Consejo:** Mantenga el código QR dentro del marco de la cámara y asegúrese de tener buena iluminación.

### Check-in mediante Deep Link

Además del escaneo tradicional, MobileLantern puede procesar códigos QR que contengan enlaces especiales (Deep Links). El profesor siempre compartirá un código QR, nunca un enlace directo.

**Cómo Funciona:**

1. **La aplicación detecta automáticamente** el formato del código
2. **Procesa las credenciales** sin mostrar la cámara
3. **Muestra un indicador de carga** mientras procesa
4. **Inicia sesión automáticamente** si las credenciales son válidas
5. **Redirige a la pantalla de preguntas** al completar

> **💡 Nota:** Desde su perspectiva como estudiante, el proceso es idéntico al escaneo normal de QR. La diferencia es técnica y transparente para usted.

---

## 3. Registrar una Pregunta

Una vez autenticado, llegará a la pantalla principal donde puede registrar su duda o pregunta.

**Pasos:**
1. Escriba su pregunta o duda en el campo de texto
2. Sea específico y claro en su pregunta
3. Pulse el botón **"Registrar Pregunta"**
4. Aparecerá un diálogo indicándole que coloque el **vaso verde** en su mesa

**¿Qué sucede ahora?**
- 🔦 La **linterna** de su dispositivo se enciende (luz constante verde)
- ⏱️ El **temporizador** comienza a contar
- 📍 Su profesor puede ver que está trabajando en una pregunta

---

## 4. Fase Colaborativa (Vaso Verde)

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
4. Aparecerá el diálogo para colocar el **vaso rojo**

**¿Qué cambia?**
- 🔴 Cambie el vaso verde por el **vaso rojo**
- 🔦 La linterna comienza a **parpadear** (tintineo)
- 🤖 Se activa automáticamente la **asistencia por IA**

---

## 5. Asistencia por IA (Vaso Rojo)

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
- 👨‍🏫 **Botón flotante (FAB)**: Botón circular en la esquina inferior derecha que solicita intervención directa del profesor cuando necesita ayuda presencial

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

> **📝 Nota:** Si el profesor ha intervenido y resuelto finalmente su pregunta, puede pulsar el botón flotante (FAB) 👨‍🏫 en la esquina inferior derecha para registrar su solución.

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

1. **Primer toque**: El icono crece ligeramente (escala 1.15x)
2. **Segundo toque**: El icono crece más (escala 1.3x)
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
- Pulse el **icono de menú** en la esquina superior izquierda

**Elementos de la pantalla:**

- 🔍 **Barra de búsqueda**: Encuentre preguntas escribiendo palabras clave. Los resultados se filtran en tiempo real.
- ➕ **Botón Nueva Pregunta**: Botón azul con icono "+" para iniciar una nueva sesión o volver a la pantalla de registro.
- 📅 **Organización temporal**: Las preguntas se agrupan por "Hoy", "Ayer" y "Semana pasada".
- 📝 **Tarjetas de preguntas**: Cada pregunta muestra:
  - Icono según el estado (✓ verde para completada, 🤖 para consulta IA, 💡 para respuesta, ✋ para pendiente)
  - Texto de la pregunta (primeras líneas)
  - Estado: Completada, Consulta IA, Respuesta o Pendiente
  - Fecha de registro
- 👤 **Perfil de usuario**: En la parte inferior muestra su nombre, email y el total de preguntas. Incluye botón "Cerrar sesión".

**Ver detalles:**
Toque cualquier pregunta del historial para ver la pregunta completa, respuestas colaborativas, respuestas de la IA, intervención del profesor y la línea temporal completa.

---

## 🎓 Mejores Prácticas

### Para Estudiantes

✅ **Pruebe la linterna** en la pantalla de bienvenida antes de comenzar  
✅ **Asegúrese de que ninguna otra app** esté usando la linterna  
✅ **Intente resolver colaborativamente primero** antes de recurrir a la IA  
✅ **Sea específico** en sus preguntas tanto a compañeros como a la IA  
✅ **Documente su proceso** escribiendo las respuestas intentadas  
✅ **Use la IA para aprender**, no solo para obtener respuestas  
✅ **Solicite al profesor** cuando realmente esté bloqueado  
✅ **Revise el historial** antes de hacer preguntas repetidas  
✅ **Use el botón de silencio** solo en emergencias (requiere 3 toques)

### Para Profesores

✅ **Monitoree las luces** de los dispositivos periódicamente  
✅ **Priorice los vasos rojos** con parpadeo rápido  
✅ **Intervenga estratégicamente** cuando suenen las alertas  
✅ **Revise el historial** después de clase para identificar patrones  
✅ **Ajuste el contexto pedagógico** según las necesidades del curso  
✅ **Fomente el uso colaborativo** antes de recurrir a la IA  
✅ **Use el Modo Aula** para ver todas las preguntas de la clase  
✅ **Genere códigos QR con anticipación** y asegúrese de que sean legibles

---

## Próximos pasos

- Consulta las [preguntas frecuentes]({{< ref "faq" >}}) para dudas comunes
- Revisa [qué es MobileLantern]({{< ref "que-es" >}}) para entender mejor el sistema
