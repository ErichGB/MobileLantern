---
title: "Instalación"
date: 2025-01-25T12:00:00Z
draft: false
weight: 3
description: "Cómo instalar y configurar la aplicación MobileLantern"
---

# 💻 Instalación y Requisitos

Esta guía te ayudará a instalar la aplicación móvil MobileLantern en tu dispositivo Android.

## Requisitos del Sistema

### Hardware Mínimo

- Dispositivo Android con **SDK 28 (Android 9)** o superior
- Cámara trasera funcional para escaneo QR
- Linterna (LED flash) funcional
- Mínimo **2GB de RAM**
- **100MB** de espacio disponible

### Recomendado

- **Android 13** o superior (SDK 35)
- **4GB de RAM** o más
- Conexión **WiFi estable**

## Permisos Necesarios

La aplicación solicitará los siguientes permisos:

| Permiso | Propósito | Obligatorio |
|---------|-----------|-------------|
| 📷 **Cámara** | Escanear código QR de acceso | ✅ Sí |
| 🔦 **Linterna** | Sistema de señalización visual | ✅ Sí |
| 🌐 **Internet** | Comunicación con IA y servidor | ✅ Sí |

---

## Descarga e Instalación

### Descarga Directa (APK)

1. Descargue el archivo APK desde el siguiente enlace:
   
   <div x-data x-init="$store.download.init()" class="my-4">
     <div x-show="$store.download.loading" class="text-slate-600 dark:text-slate-400">
       Cargando información de descarga...
     </div>
     <div x-show="!$store.download.loading && $store.download.downloadUrl">
       <a 
         :href="$store.download.downloadUrl" 
         @click="$store.download.download()"
         target="_blank"
         rel="noopener noreferrer"
         class="text-violet-600 dark:text-violet-400 hover:text-violet-700 dark:hover:text-violet-300 underline">
         <span x-text="'Descargar APK' + ($store.download.version ? ' (v' + $store.download.version + ')' : '')"></span>
       </a>
     </div>
     <div x-show="!$store.download.loading && !$store.download.downloadUrl" class="text-red-600 dark:text-red-400">
       No se tiene información de descarga. Por favor, intente más tarde.
     </div>
   </div>

2. Active "Orígenes desconocidos" en su dispositivo:
   - Vaya a **Configuración** > **Seguridad**
   - Active **Fuentes desconocidas** o **Instalar aplicaciones desconocidas**

3. Abra el archivo APK descargado

4. Siga las instrucciones de instalación en pantalla

---

## Primera Ejecución

### Otorgar Permisos

La primera vez que abras MobileLantern, la aplicación te pedirá los permisos necesarios:

1. **Permiso de cámara**
   - Selecciona: **"Permitir solo mientras se usa la app"** o **"Permitir"**
   - Necesario para escanear códigos QR

2. **Permiso de linterna** (si aplica)
   - Generalmente incluido con permisos de cámara
   - Necesario para el ambient display

> ⚠️ **Importante**: Si deniega algún permiso, algunas funciones no estarán disponibles. Puedes cambiar los permisos más tarde en la configuración del sistema.

### Verificar Instalación

Para confirmar que la instalación fue exitosa:

1. Abre la aplicación MobileLantern
2. Deberías ver la pantalla de bienvenida
3. El botón **"Empezar"** debe estar visible

---

## Solución de Problemas

### No puedo instalar el APK

**Problema**: El sistema no permite la instalación.

**Solución**:
- Verifica que hayas habilitado la instalación desde fuentes desconocidas
- Asegúrate de que el APK se haya descargado completamente
- Intenta descargar el APK de nuevo

### La aplicación no abre

**Problema**: La app se cierra inmediatamente después de abrirla.

**Solución**:
- Verifica que tu versión de Android sea **9.0 o superior**
- Reinicia tu dispositivo
- Desinstala y reinstala la aplicación
- Contacta con soporte técnico si el problema persiste

### No funciona la cámara

**Problema**: No puedo escanear códigos QR.

**Solución**:
- Ve a **Configuración** → **Aplicaciones** → **MobileLantern** → **Permisos**
- Asegúrate de que el permiso de **Cámara** esté activado
- Reinicia la aplicación

### La linterna no se activa

**Problema**: El ambient display no muestra luces.

**Solución**:
- Verifica que tu dispositivo tenga linterna funcional
- Comprueba que la linterna no esté siendo usada por otra aplicación
- Otorga todos los permisos necesarios

---

## Actualización

Para actualizar MobileLantern a una nueva versión:

1. Descarga el nuevo APK desde la página principal
2. Instala sobre la versión existente
3. No es necesario desinstalar la versión anterior
4. Tus datos y configuración se conservarán

---

## Desinstalación

Si necesitas desinstalar MobileLantern:

1. Ve a **Configuración** → **Aplicaciones**
2. Busca y selecciona **MobileLantern**
3. Toca **Desinstalar**
4. Confirma la acción

> **Nota**: Al desinstalar perderás el historial local, pero los datos en el servidor se conservan.

---

## Próximos pasos

Una vez instalada la aplicación:

- Lee sobre el [uso básico]({{< ref "uso-basico" >}}) para comenzar
- Consulta las [preguntas frecuentes]({{< ref "faq" >}}) si tienes dudas
