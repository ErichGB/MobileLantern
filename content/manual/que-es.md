---
title: "¿Qué es MobileLantern?"
date: 2025-01-25T12:00:00Z
draft: false
weight: 2
description: "Conoce el sistema MobileLantern y su arquitectura pedagógica"
---

# ¿Qué es MobileLantern?

MobileLantern es un **sistema de orquestación de clase** que integra ambient display con inteligencia artificial para facilitar el aprendizaje colaborativo en entornos educativos.

## Propósito del sistema

El proyecto forma parte de las líneas de investigación de la Universitat Pompeu Fabra y la EPFL sobre orquestación de clases mediante dispositivos ambientales inspirados en Lantern, combinados con inteligencia artificial generativa para ofrecer apoyo inmediato al alumnado durante actividades colaborativas.

### Objetivos principales

- **Transformar la interacción** docente-estudiante mediante dispositivos físicos
- **Fomentar el trabajo colaborativo** antes de solicitar ayuda externa
- **Proporcionar asistencia inmediata** mediante IA cuando sea necesario
- **Optimizar el tiempo del docente** permitiéndole enfocarse en casos complejos

## Arquitectura pedagógica

MobileLantern implementa el concepto de **"vasos de aprendizaje"** con señalización visual y sonora:

### 🟢 Vaso Verde (Fase colaborativa)

Cuando un grupo formula una duda, el sistema activa el **vaso verde**, indicando que el equipo está trabajando en resolver el problema de forma colaborativa. Durante esta fase:

- El grupo tiene un tiempo limitado para encontrar la solución
- Se fomenta la discusión y el intercambio de ideas
- El ambient display emite luz verde y señales sonoras suaves

### 🔴 Vaso Rojo (Asistencia necesaria)

Si el grupo no logra resolver la duda en el tiempo establecido, el sistema cambia al **vaso rojo**:

- Se activa la asistencia de IA con respuestas contextualizadas
- El ambient display emite luz roja intermitente
- El grupo puede solicitar la intervención del docente si la IA no es suficiente

## Componentes del sistema

### 1. Aplicación móvil (Mobile Lantern)

- Desarrollada en Kotlin con Jetpack Compose
- Interfaz intuitiva para estudiantes
- Gestión de preguntas y respuestas
- Control del hardware local (linterna, alertas)

### 2. Ambient Display

- Dispositivo físico que usa el teléfono móvil
- Señales visuales (luz de linterna)
- Alertas sonoras según el estado del grupo
- Comunicación clara del estado actual

### 3. Inteligencia Artificial

- Asistencia contextualizada mediante IA generativa
- Respuestas pedagógicas que guían sin dar soluciones directas
- Adaptación al contexto definido por el docente

### 4. Backend de orquestación

- API REST para gestión de sesiones
- Almacenamiento de historial de preguntas
- Coordinación entre todos los componentes

## Flujo de trabajo típico

1. **Inicio de sesión**
   - El profesor genera códigos QR para cada grupo
   - Los estudiantes escanean el QR para hacer check-in
   - El sistema registra al grupo en la sesión activa

2. **Formulación de pregunta**
   - Un estudiante plantea una duda a través de la app
   - Se activa el vaso verde y comienza el temporizador
   - El ambient display muestra el estado al grupo

3. **Resolución colaborativa**
   - El grupo trabaja en conjunto durante el tiempo del vaso verde
   - Pueden registrar su respuesta en la aplicación
   - Si resuelven el problema, el ciclo se cierra

4. **Asistencia de IA**
   - Si no resuelven en el tiempo establecido, se activa el vaso rojo
   - La IA proporciona orientación contextualizada
   - El grupo puede iterar con más preguntas a la IA

5. **Intervención docente**
   - Si la IA no es suficiente, pueden solicitar al profesor
   - El docente recibe notificación de la necesidad de intervención
   - La interacción queda registrada para análisis posterior

## Beneficios educativos

- **Autonomía del estudiante**: Fomenta la búsqueda de soluciones antes de pedir ayuda
- **Aprendizaje entre pares**: Promueve la discusión y colaboración en grupo
- **Escalabilidad**: Permite atender múltiples grupos simultáneamente
- **Retroalimentación inmediata**: La IA proporciona guía cuando se necesita
- **Datos para mejora**: Registra el proceso de aprendizaje para análisis

