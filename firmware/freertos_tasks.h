// ====================================================================
// freertos_tasks.h — Tareas concurrentes FreeRTOS para LED y buzzer
// ====================================================================
// Infraestructura de colas, mutex y tipos de comando para la ejecución
// concurrente del anillo WS2812B y el buzzer pasivo sobre ESP32.
//
// Arquitectura:
//   loop()       [Core 1] — BT serial, dispatch de comandos, OTA
//   vLedTask     [Core 1] — Efectos y color del anillo WS2812B
//   vBuzzerTask  [Core 1] — Melodías, beeps y tonos del buzzer
//
// Comunicación entre tareas: colas FreeRTOS (ledQueue, buzzerQueue)
// Sincronización: stateMutex para acceso cruzado al estado compartido
// ====================================================================

#ifndef FREERTOS_TASKS_H
#define FREERTOS_TASKS_H

#include <Arduino.h>
#include <freertos/queue.h>
#include <freertos/semphr.h>

#define LED_TASK_STACK    4096
#define BUZZER_TASK_STACK 4096
#define LED_TASK_PRIO     1
#define BUZZER_TASK_PRIO  1
#define CMD_QUEUE_DEPTH   8

// ====== Tipos de comando LED ======
enum LedCmdType {
  LED_CMD_ON,
  LED_CMD_OFF,
  LED_CMD_COLOR,
  LED_CMD_BRIGHTNESS,
  LED_CMD_EFFECT,
  LED_CMD_BLINK,
  LED_CMD_STOP
};

struct LedCmd {
  LedCmdType type;
  uint8_t r, g, b;
  uint8_t brightness;
  uint8_t effect;
};

// ====== Tipos de comando buzzer ======
enum BuzzerCmdType {
  BUZZER_CMD_ON,
  BUZZER_CMD_OFF,
  BUZZER_CMD_BEEP,
  BUZZER_CMD_TONE,
  BUZZER_CMD_STOP
};

enum ToneId {
  TONE_ID_WELCOME,
  TONE_ID_GOODBYE,
  TONE_ID_NOTIFY,
  TONE_ID_SUCCESS,
  TONE_ID_ALERT
};

struct BuzzerCmd {
  BuzzerCmdType type;
  uint8_t beepCount;
  ToneId tone;
};

// ====== Handles globales ======
QueueHandle_t ledQueue = NULL;
QueueHandle_t buzzerQueue = NULL;
SemaphoreHandle_t stateMutex = NULL;

#endif // FREERTOS_TASKS_H
