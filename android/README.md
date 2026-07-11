# MobileLantern —  Android

Aplicación Android del ecosistema MobileLantern. Desarrollada con **Kotlin**, **Jetpack Compose** y **Hilt**.

## Requisitos

- Android Studio Ladybug o superior
- JDK 11+
- SDK Android 35 (min SDK 28)

## Configuración

Copia el archivo de ejemplo y rellena tus valores:

```bash
cp local.properties.example local.properties
```

| Clave | Descripción |
|-------|-------------|
| `sdk.dir` | Ruta al Android SDK |
| `BASE_URL` | URL del backend |
| `BASIC_USER` | Usuario de autenticación básica |
| `BASIC_PASS` | Contraseña de autenticación básica |
| `KEYSTORE_FILE` | Ruta al keystore de release |
| `KEYSTORE_PASSWORD` | Contraseña del keystore |
| `KEY_ALIAS` | Alias de la clave |
| `KEY_PASSWORD` | Contraseña de la clave |

> Los secretos también se pueden proporcionar como variables de entorno con el mismo nombre.

## Build

```bash
./gradlew assembleDebug
```

Para release (requiere keystore configurado):

```bash
./gradlew assembleRelease
```

## Arquitectura

```
app/src/main/java/com/mssde/mobilelantern/
├── data/           # Repositorios, modelos, Room, Retrofit
├── di/             # Módulos Hilt (Database, Hardware, Network, Repository)
├── domain/         # Casos de uso
├── hardware/       # Integración con linterna física (BLE/ESP32)
├── ui/             # Composables y pantallas
├── viewmodel/      # ViewModels
└── *Activity.kt    # Activities principales
```

## Stack

Kotlin · Jetpack Compose · Hilt · Retrofit · Room · CameraX · ZXing

