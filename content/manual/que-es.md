---
title: "¿Qué es MobileLantern?"
date: 2025-01-25T12:00:00Z
draft: false
weight: 2
description: "Conoce el sistema MobileLantern, generador de QR y sistema de señalización"
---

# ¿Qué es MobileLantern?

MobileLantern es un **sistema de orquestación de clase** que integra ambient display con inteligencia artificial para facilitar el aprendizaje colaborativo en entornos educativos.

## Propósito del sistema

El proyecto forma parte de las líneas de investigación de la Universidad Politécnica de Madrid (UPM) y la Universitat Pompeu Fabra (UPF) sobre orquestación de clases mediante dispositivos ambientales, combinados con inteligencia artificial generativa para ofrecer apoyo inmediato al alumnado durante actividades colaborativas.

### Objetivos principales

- **Transformar la interacción** docente-estudiante mediante dispositivos físicos
- **Fomentar el trabajo colaborativo** antes de solicitar ayuda externa
- **Proporcionar asistencia inmediata** mediante IA cuando sea necesario
- **Optimizar el tiempo del docente** permitiéndole enfocarse en casos complejos

---

## 🔗 Generador de Códigos QR para Profesores

### ¿Qué es el Generador de QR?

El **Generador de Códigos QR** es una herramienta web integrada que permite a los profesores crear códigos QR personalizados para sus sesiones de clase. Esta herramienta está disponible en línea y no requiere instalación adicional.

### Acceso al Generador

**URL:** [Generador de QR](/qr-generator)

### Componentes del Código QR

Cada código QR generado contiene los siguientes elementos:

#### 1. **Usuario del Grupo** (Obligatorio)
- Identificador único para el grupo de trabajo
- Ejemplo: `grupo1`, `equipo-a`, `estudiantes-2025`
- **Propósito:** Organizar y diferenciar los grupos en la sesión

#### 2. **Contraseña del Grupo** (Obligatorio)
- Clave de acceso para el grupo específico
- Puede ser la misma para todos los grupos o diferente según la estrategia pedagógica
- **Propósito:** Controlar el acceso y mantener la seguridad de la sesión

#### 3. **Contexto Pedagógico** (Opcional)
- Instrucciones específicas para configurar el comportamiento de la IA
- Define el rol, estilo de enseñanza y enfoque pedagógico
- **Propósito:** Personalizar las respuestas de la IA según la materia y metodología

#### 4. **Identificador de Aula** (Opcional)
- Número identificador del aula o clase
- Ejemplo: `35`, `101`, `aula-principal`
- **Propósito:** Habilitar el **Modo Aula** para que el profesor pueda ver el historial completo de todos los grupos

> **📝 Nota sobre el Modo Aula:** Cuando se incluye el campo `aula` en el código QR, el historial de la aplicación mostrará las preguntas de **todos los grupos del aula**, no solo las del grupo individual. Esto es especialmente útil para profesores que desean monitorear el progreso de toda la clase.

### Cómo Usar el Generador

1. **Acceda a la herramienta:** Navegue al [Generador de QR](/qr-generator) en su navegador web

2. **Configure las credenciales:**
   - **Usuario:** Ingrese el identificador del grupo (ej: `grupo1`, `equipo-matematicas`)
   - **Contraseña:** Defina la clave de acceso (ej: `clase2024`, `sesion01`)
   - **Contexto:** (Opcional) Instrucciones para configurar el comportamiento de la IA según la materia y metodología
   - **Aula:** (Opcional) Identificador del aula para habilitar el Modo Aula

3. **Genere el código QR:** Haga clic en **"Generar Código QR"** y use el botón **"Descargar"** para guardar la imagen

### Mejores Prácticas para Profesores

#### Preparación de la Sesión
✅ **Planifique con anticipación:** Genere los códigos QR antes de la clase  
✅ **Pruebe el contexto:** Verifique que las instrucciones de IA sean apropiadas  
✅ **Prepare múltiples códigos:** Tenga códigos diferentes para distintos grupos si es necesario  
✅ **Imprima o proyecte:** Asegúrese de que todos los estudiantes puedan escanear el código

#### Gestión de Grupos
✅ **Nombres descriptivos:** Use identificadores claros para los grupos  
✅ **Contraseñas simples:** Evite caracteres especiales que puedan causar problemas  
✅ **Documentación:** Mantenga un registro de qué código corresponde a cada grupo

#### Contexto Pedagógico Efectivo
✅ **Sea específico:** Defina claramente el rol y comportamiento esperado de la IA  
✅ **Adapte al nivel:** Ajuste el lenguaje y complejidad según el nivel educativo  
✅ **Incluya restricciones:** Especifique qué NO debe hacer la IA (ej: no dar respuestas directas)  
✅ **Considere la materia:** Personalice según las necesidades específicas de la asignatura

> **💡 Consejo para profesores:** Use el modo aula para generar un código QR especial que le permita ver todas las preguntas de la clase en el historial.

### Solución de Problemas del Generador

| Problema | Causa | Solución |
|----------|-------|----------|
| 🚫 No genera QR | Campos vacíos | Complete todos los campos requeridos |
| 📱 QR no escanea | Tamaño muy pequeño | Descargue la imagen en alta resolución |
| 🤖 IA no responde bien | Contexto unclear | Revise y mejore las instrucciones del contexto |
| 🔒 Error de acceso | Credenciales incorrectas | Verifique usuario y contraseña en el QR |

---

## 🚦 Sistema de Señalización Visual

MobileLantern utiliza un sistema de señales visuales para comunicar el estado de cada grupo sin interrumpir la clase.

### Estados de la Linterna

| Estado | Descripción | Significado |
|--------|-------------|-------------|
| 🔦 **Luz Constante** | Linterna encendida sin parpadear | Fase colaborativa (filtro verde) |
| 💡 **Parpadeo** | ~1 parpadeo por segundo | Fase IA activa tras 3 min (filtro rojo) |
| ⚫ **Apagada** | Sin luz | Sin actividad / pregunta resuelta |

### Sistema de Filtros

#### 🟢 Filtro Verde
- **Cuándo usarlo:** Durante la fase colaborativa
- **Significado:** "Estamos trabajando en una pregunta"
- **Para el profesor:** El grupo está colaborando, no requiere intervención inmediata

#### 🔴 Filtro Rojo
- **Cuándo usarlo:** Cuando no se pudo resolver colaborativamente
- **Significado:** "Necesitamos ayuda adicional"
- **Para el profesor:** El grupo está usando IA, monitorear por si necesitan intervención

> **📡 Linterna externa Bluetooth:** Con la linterna externa el color del LED (verde/rojo) cambia automáticamente y no se muestran los diálogos de "Coloca tu filtro". Consulta los detalles en la [guía de la linterna externa]({{< ref "linterna-externa" >}}).

### Frecuencia de Parpadeo

Durante toda la fase IA la linterna parpadea a una frecuencia fija de **~1 Hz** (1 parpadeo por segundo).

En su lugar, la presión sobre el grupo aumenta mediante **alertas acústicas** a partir de los 6 minutos en fase IA:

| Tiempo en Fase IA | Señalización |
|-------------------|--------------|
| 0-3 minutos | Sin alertas (solo temporizador visual) |
| 3-6 minutos | Parpadeo visual (~1 Hz, sin sonido) |
| 6-7 minutos | 1 pitido cada **30 s** |
| 7-8 minutos | 1 pitido cada **20 s** |
| >8 minutos | 1 pitido cada **10 s** |

Esto ayuda al profesor a **identificar** qué grupos llevan más tiempo en fase IA por la combinación de parpadeo y pitidos.

---

## 🤖 Asistencia por Inteligencia Artificial

### Cómo Hacer Buenas Preguntas a la IA

#### ✅ Ejemplos de Buenas Preguntas

```
❓ "¿Cómo puedo optimizar esta consulta SQL que tarda mucho?"

❓ "Explícame el concepto de recursividad con un ejemplo simple"

❓ "Mi código da el error 'NullPointerException' en la línea 45. ¿Qué significa?"

❓ "¿Cuál es la diferencia entre == y equals() en Java?"
```

#### ❌ Ejemplos de Preguntas Poco Efectivas

```
❌ "No funciona" (demasiado vago)

❌ "Ayuda" (sin contexto)

❌ "¿Qué hago?" (sin especificar el problema)
```

### Límites y Alertas

Para fomentar la resolución colaborativa y evitar dependencia excesiva de la IA, el sistema avisa visual y acústicamente conforme avanza el tiempo en fase IA:

- **A los 3 minutos** en fase IA → la linterna **comienza a parpadear** (filtro rojo).
- **A los 6 minutos** → se activan **alertas acústicas** periódicas, cada vez más frecuentes (cada 30 s, luego 20 s y, a partir del tercer minuto, cada 10 s).
- 💬 Puede seguir usando la IA, pero se recomienda solicitar **intervención del profesor**.

> **📝 Nota:** No existe un límite estricto de "número de intentos". El sistema se basa en el tiempo total en fase IA para recordarle que valore pedir ayuda presencial.

---

## Beneficios educativos

- **Autonomía del estudiante**: Fomenta la búsqueda de soluciones antes de pedir ayuda
- **Aprendizaje entre pares**: Promueve la discusión y colaboración en grupo
- **Escalabilidad**: Permite atender múltiples grupos simultáneamente
- **Retroalimentación inmediata**: La IA proporciona guía cuando se necesita
- **Datos para mejora**: Registra el proceso de aprendizaje para análisis
