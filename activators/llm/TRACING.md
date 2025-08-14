# LLM Tracing в JAICF

## Обзор

JAICF поддерживает трассировку LLM вызовов через несколько систем:
- **LangSmith** - для мониторинга и анализа LLM вызовов
- **OpenTelemetry** - для интеграции с системами observability

## Проблема множественных LLM Call'ов

При запуске тестов с трассировкой LangSmith может создаваться множество отдельных LLM Call'ов вместо одного логического run'а. Это происходит потому, что каждый вызов `send()` или `query()` в тесте создает отдельный LLM call.

### Пример проблемы

```kotlin
@Test
fun `agent achieves goal`() = testWithLLM {
    send(user = "Hello", agent = "Greets and asks firstname")  // LLM Call #1
    send(user = "John", agent = "Asks the lastname")           // LLM Call #2
    send(user = "Doe", agent = "Asks for age")                // LLM Call #3
    query("Thirty") returnsResult Goal("John", "Doe", Optional.of(30)) // LLM Call #4
}
```

Результат: 4 отдельных LLM Call'а в LangSmith вместо одного логического run'а.

## Решение: Test Chain Tracing

Мы реализовали **Test Chain Tracing**, который создает один chain run для всего теста, а все LLM calls становятся дочерними от него.

### Как это работает

1. **Создание Test Chain Run**: В начале теста создается один chain run
2. **Дочерние LLM Calls**: Все последующие LLM calls создаются как дочерние от test chain run
3. **Завершение**: В конце теста test chain run завершается

### Архитектура

```
Test Chain Run (Chain)
├── LLM Call #1 (LLM)
├── LLM Call #2 (LLM)
├── LLM Call #3 (LLM)
└── LLM Call #4 (LLM)
```

### Реализация

#### 1. TracingManager

```kotlin
fun startTestChainRun(
    context: BotContext,
    request: BotRequest,
    testName: String
): Map<String, String>

fun endTestChainRun(
    runIds: Map<String, String>,
    outputs: Map<String, Any>
)
```

#### 2. LangSmithTracer

```kotlin
override fun startTestChainRun(
    context: BotContext,
    request: BotRequest,
    testName: String
): String {
    // Создает chain run в LangSmith
    return client.createRun(
        runId = runId,
        name = "Test Chain: $testName",
        runType = "chain",
        inputs = inputs,
        startTime = startTime
    )
}
```

#### 3. Модификация LLM Calls

```kotlin
override fun startLLMRun(
    context: BotContext,
    request: BotRequest,
    props: LLMProps,
    messages: List<Map<String, Any>>
): String {
    // Проверяем, есть ли test chain run ID
    val testChainRunId = context.temp["tracing.test_chain_run_id"] as? String
    
    val success = if (testChainRunId != null) {
        // Создаем дочерний run под test chain
        client.createChildRun(
            runId = runId,
            parentRunId = testChainRunId,
            name = "LLM Call",
            runType = "llm",
            inputs = inputs,
            startTime = startTime
        )
    } else {
        // Создаем standalone run
        client.createRun(...)
    }
}
```

#### 4. LLMTest

```kotlin
class LLMTest(testAgent: LLMAgent) {
    private var testChainRunIds: Map<String, String>? = null

    private fun BotTest.process(input: String): ProcessResult {
        // Создаем test chain run при первом вызове
        if (testChainRunIds == null) {
            val tracingManager = TracingManager.get()
            testChainRunIds = tracingManager.startTestChainRun(
                context.botContext, 
                request, 
                "LLM Test: ${this::class.java.simpleName}"
            )
            
            // Устанавливаем test chain run ID в контекст
            testChainRunIds?.let { runIds ->
                val firstRunId = runIds.values.firstOrNull()
                if (firstRunId != null) {
                    context.botContext.temp["tracing.test_chain_run_id"] = firstRunId
                }
            }
        }
        
        // ... остальная логика
    }
}
```

## Использование

### 1. Включение трассировки

Установите переменные окружения:

```bash
export LANGSMITH_API_KEY="your_api_key"
export LANGSMITH_PROJECT="your_project"
export LANGSMITH_TRACING="true"
```

### 2. Запуск тестов

```kotlin
@Test
fun `test with tracing`() = testWithLLM {
    send(user = "Hello", agent = "Greets user")
    send(user = "How are you?", agent = "Asks about well-being")
}
```

### 3. Результат в LangSmith

Вместо множественных LLM Call'ов вы увидите:

```
Test Chain: AgentWithGoalTest (Chain)
├── LLM Call (LLM) - "Hello"
├── LLM Call (LLM) - "How are you?"
└── Test completed
```

## Преимущества

1. **Логическая группировка**: Все LLM calls в тесте группируются в один chain
2. **Лучшая аналитика**: Можно анализировать производительность всего теста
3. **Структурированность**: Четкая иерархия runs в LangSmith
4. **Обратная совместимость**: Существующие тесты работают без изменений

## Ограничения

1. **Только для тестов**: Test Chain Tracing работает только в контексте `testWithLLM`
2. **LangSmith**: Требует настройки LangSmith API
3. **OpenTelemetry**: Поддерживается, но с ограниченной функциональностью

## Будущие улучшения

1. **Автоматическое определение тестов**: Автоматическое создание test chain runs
2. **Метрики тестов**: Добавление метрик производительности тестов
3. **Интеграция с CI/CD**: Автоматическая трассировка в CI/CD pipeline
4. **Кастомные теги**: Возможность добавления кастомных тегов к test chain runs
