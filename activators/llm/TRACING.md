# LLM Tracing System

Система трейсинга для LLM активатора JAICF, поддерживающая интеграцию с LangSmith и OpenTelemetry.

## Обзор

Система трейсинга позволяет отслеживать и анализировать:
- LLM вызовы (запросы и ответы)
- Вызовы инструментов (tools)
- Цепочки операций (chains)
- Использование токенов и метаданные

## Поддерживаемые системы трейсинга

### 1. LangSmith

LangSmith - это платформа для отслеживания, отладки и оценки LLM приложений.

#### Настройка

Установите переменные окружения:

```bash
# Основной API ключ LangSmith
export LANGCHAIN_API_KEY="your-api-key-here"

# Опционально: проект для группировки трейсов
export LANGCHAIN_PROJECT="your-project-name"
```

#### Что отслеживается

- **LLM вызовы**: модель, параметры, входные сообщения, ответы, использование токенов
- **Вызовы инструментов**: название инструмента, аргументы, результаты
- **Цепочки**: последовательности операций с метаданными

### 2. OpenTelemetry

OpenTelemetry - стандарт для телеметрии в распределенных системах.

#### Настройка

Установите переменные окружения:

```bash
# Включить OpenTelemetry
export OTEL_TRACES_ENABLED="true"

# Endpoint для отправки трейсов (опционально)
export OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4317"

# Или использовать ADK
export ADK_TELEMETRY="otel"
```

## Использование

### Автоматическое включение

Трейсинг автоматически включается при наличии соответствующих переменных окружения.

### Ручное управление

```kotlin
import com.justai.jaicf.activator.llm.tracing.*

// Создание конфигурации
val langSmithConfig = LangSmithConfig.create(
    apiKey = "your-api-key",
    project = "your-project"
)

val oTelConfig = OpenTelemetryConfig(
    enabled = true,
    endpoint = "http://localhost:4317"
)

// Инициализация трассировщиков
val langSmithTracer = LangSmithTracer(langSmithConfig)
val oTelTracer = OpenTelemetryTracer(oTelConfig)

// Добавление в менеджер
val manager = TracingManager.get()
manager.addTracer(langSmithTracer)
manager.addTracer(oTelTracer)
```

### Интеграция с LLM контекстом

Трейсинг автоматически интегрируется в следующие методы:

- `LLMContext.chatCompletion()` - отслеживание LLM вызовов
- `LLMContext.callTool()` - отслеживание вызовов инструментов
- `LLMContext.withToolCalls()` - отслеживание цепочек инструментов

## Структура трейсов

### LLM Run

```json
{
  "id": "langsmith_timestamp_clientId",
  "name": "LLM Call",
  "run_type": "llm",
  "inputs": {
    "model": "gpt-3.5-turbo",
    "temperature": 0.7,
    "messages": [...],
    "bot_context_id": "...",
    "request_id": "..."
  },
  "outputs": {
    "content": "LLM response content",
    "finish_reason": "stop",
    "choices_count": 1,
    "model": "gpt-3.5-turbo"
  }
}
```

### Tool Run

```json
{
  "id": "langsmith_tool_timestamp_toolId",
  "name": "Tool Call: tool_name",
  "run_type": "tool",
  "inputs": {
    "tool_name": "get_weather",
    "arguments": {...}
  },
  "outputs": {
    "tool_name": "get_weather",
    "tool_result": "Weather data",
    "success": true
  }
}
```

### Chain Run

```json
{
  "id": "langsmith_chain_timestamp_clientId",
  "name": "Chain: Tool Calls Chain",
  "run_type": "chain",
  "inputs": {
    "chain_name": "Tool Calls Chain",
    "bot_context_id": "..."
  },
  "outputs": {
    "tool_calls_count": 2,
    "tool_names": ["get_weather", "format_response"],
    "success": true
  }
}
```

## Мониторинг и отладка

### Логи

Система трейсинга выводит подробные логи:

```
LangSmith: Started LLM run langsmith_1234567890_client123
LangSmith: LLM run inputs - model: gpt-3.5-turbo, messages: [...]
LangSmith: Ended LLM run langsmith_1234567890_client123
```

### Ошибки

При ошибках трейсинга система продолжает работать, логируя проблемы:

```
LangSmith: Failed to create run runId, status: 401, body: Unauthorized
LangSmith: Error creating run runId: java.net.ConnectException
```

## Производительность

- Трейсинг выполняется асинхронно и не блокирует основной поток
- HTTP клиенты используют пулы соединений
- Настраиваемые таймауты для предотвращения блокировки
- Graceful degradation при недоступности сервисов трейсинга

## Конфигурация

### LangSmith

```kotlin
data class LangSmithConfig(
    val enabled: Boolean = false,
    val apiKey: String? = null,
    val project: String? = null,
    val endpoint: String = "https://api.smith.langchain.com",
    val timeout: Long = 30000L
)
```

### OpenTelemetry

```kotlin
data class OpenTelemetryConfig(
    val enabled: Boolean = false,
    val endpoint: String? = null,
    val serviceName: String = "jaicf-llm"
)
```

## Примеры использования

### Простой бот с трейсингом

```kotlin
val bot = Bot(
    model = "gpt-3.5-turbo",
    temperature = 0.7
) {
    // Трейсинг автоматически включится при наличии LANGCHAIN_API_KEY
    // или OTEL_TRACES_ENABLED
}
```

### Кастомная конфигурация трейсинга

```kotlin
val langSmithConfig = LangSmithConfig.create(
    apiKey = "custom-key",
    project = "custom-project"
)

val oTelConfig = OpenTelemetryConfig(
    enabled = true,
    endpoint = "http://custom-otel-endpoint:4317"
)

// Инициализация трассировщиков
val manager = TracingManager.get()
manager.addTracer(LangSmithTracer(langSmithConfig))
manager.addTracer(OpenTelemetryTracer(oTelConfig))
```

## Устранение неполадок

### LangSmith не работает

1. Проверьте переменную `LANGCHAIN_API_KEY`
2. Убедитесь, что API ключ действителен
3. Проверьте доступность `https://api.smith.langchain.com`

### OpenTelemetry не работает

1. Проверьте переменную `OTEL_TRACES_ENABLED`
2. Убедитесь, что endpoint доступен
3. Проверьте логи на наличие ошибок подключения

### Общие проблемы

- **Трейсинг не включается**: проверьте переменные окружения
- **Ошибки сети**: проверьте таймауты и доступность сервисов
- **Высокое потребление памяти**: настройте размеры пулов соединений

## Разработка

### Добавление нового трассировщика

1. Реализуйте интерфейс `Tracer`
2. Добавьте конфигурацию в `TracingConstants`
3. Интегрируйте в `TracingManager`

### Расширение существующих трассировщиков

- Добавьте новые атрибуты в `TracingConstants`
- Расширьте методы трейсинга в `LLMContextTracing`
- Обновите тесты в `TracingTest`

## Лицензия

Система трейсинга является частью JAICF и распространяется под той же лицензией.
