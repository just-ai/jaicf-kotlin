# Telemetry Example - Quick Start

## Что это?

Пример показывает, как использовать OpenTelemetry с JAICF BotEngine для трассировки и мониторинга работы бота.

## Запуск примера

### 1. Консольный пример (самый простой способ)

```bash
cd /Users/admin/IdeaProjects/jaicf-kotlin-new
./gradlew :examples:telemetry-example:run -PmainClass=com.justai.jaicf.examples.telemetry.ConsoleExampleKt
```

Этот пример:
- Создает бота с telemetry
- Отправляет несколько тестовых запросов
- Выводит трейсы в консоль

### 2. HTTP сервер пример

```bash
./gradlew :examples:telemetry-example:run -PmainClass=com.justai.jaicf.examples.telemetry.HttpServerExampleKt
```

Затем в другом терминале:

```bash
curl -X POST http://localhost:8080/bot \
  -H "Content-Type: application/json" \
  -d '{"query": "hello", "clientId": "test-user"}'
```

### 3. С визуализацией в Jaeger (опционально)

Запустите Jaeger:

```bash
docker run -d --name jaeger \
  -p 16686:16686 \
  -p 4317:4317 \
  jaegertracing/all-in-one:latest
```

Затем запустите пример и откройте http://localhost:16686

## Ключевые концепции

### 1. Включение telemetry

```kotlin
val bot = BotEngine(
    scenario = MyScenario,
    activators = arrayOf(RegexActivator)
).withTelemetry(
    OpenTelemetryTelemetryProvider(tracer)
)
```

### 2. Что автоматически трейсится

- **Жизненный цикл запроса**: начало и конец каждого запроса
- **Активация**: какой активатор обработал запрос и какое состояние было выбрано
- **Управление контекстом**: загрузка и сохранение контекста бота
- **Заполнение слотов**: этапы процесса заполнения слотов
- **Ошибки**: автоматическая запись исключений в спаны

### 3. Атрибуты спанов

Автоматически добавляются следующие атрибуты:

- `jaicf.request.type` - тип запроса
- `jaicf.request.client_id` - ID клиента
- `jaicf.session.new` - новая ли сессия
- `jaicf.activation.state` - путь к активированному состоянию
- `jaicf.activation.activator` - имя активатора
- `jaicf.duration_ms` - длительность в миллисекундах

## Структура файлов

```
examples/telemetry-example/
├── src/
│   ├── main/
│   │   └── kotlin/
│   │       └── com/justai/jaicf/examples/telemetry/
│   │           ├── TelemetryConfig.kt        # Конфигурация OpenTelemetry
│   │           ├── TelemetryScenario.kt      # Сценарий бота
│   │           ├── ConsoleExample.kt         # Консольный пример
│   │           └── HttpServerExample.kt      # HTTP сервер пример
│   └── test/
│       └── kotlin/
│           └── com/justai/jaicf/examples/telemetry/
│               └── TelemetryScenarioTest.kt  # Тесты
├── build.gradle.kts                          # Конфигурация сборки
├── README.md                                 # Подробная документация
├── TELEMETRY_GUIDE.md                        # Руководство по telemetry
└── QUICKSTART.md                             # Этот файл
```

## Полезные ссылки

- [README.md](README.md) - подробная документация
- [TELEMETRY_GUIDE.md](TELEMETRY_GUIDE.md) - глубокое руководство по telemetry
- [OpenTelemetry Docs](https://opentelemetry.io/docs/)
- [JAICF Documentation](https://github.com/just-ai/jaicf-kotlin)



