## Телеметрия для LLM-активатора JAICF

Цель — дать наблюдаемость для LLM-вызовов, tool calling, streaming и агентных handoff, не меняя сценарии. 
Подход сочетает существующие механизмы JAICF (`TelemetryProvider`, hook-и) и 
архитектуру koog (`SpanProcessor`-подобный lifecycle менеджер спанов), а также встроенные точки расширения 
LLM-активатора (`LLMHooks`, `LLMActionContext`).

### Цели и принципы
- Минимальная инвазия: включается через `BotEngine.withTelemetry` и опциональный `LLMTelemetryFeature`.
- Единая иерархия спанов для: Agent → LLM Call → Streaming → ToolCalls → ToolCall → VectorStore → Handoff.
- Контекстная пропагация через все границы: агент→агент, агент→tool, tool→обратный LLM.
- Безопасность по умолчанию: контент редактируется, семплируется; payload-инструментов агрегируется, а не
- логируется целиком.
- Совместимость с JAICF core-телеметрией: один `TelemetryProvider`, общие правила статусов/ошибок/атрибутов.

### Модель спанов и событий
Имена условные, провайдер может маппить их на свою нотацию.

- Span: `Agent`
  - attrs: `agent.name`, `state.path`, `session.id`, `request.id`, `chain.depth`
- Span: `LLM Call` (child of Agent)
  - attrs.in: `model`, `temperature`, `top_p`, `max_tokens`, `tools.declared`, `memory.enabled`, `vector_store.used`, `response_format`
  - attrs.req: `input.msgs.count`, `input.tokens.prompt` (если доступно), `input.bytes.approx`
  - attrs.out: `finish_reason`, `output.msgs.count`, `output.tokens.completion`, `output.tokens.total`, `tool_calls.count`
  - timing: `latency.ms`
  - status/error: `error.kind` (timeout, throttling, provider_error, internal), `error.message`
- Span: `Streaming` (child of LLM Call)
  - attrs: `chunks.count`, `streamed.tokens`, `first_byte_latency.ms`, `duration.ms`
  - events: `Start`, `ContentDelta` (редактированный/семплированный), `ToolCalls`, `ToolCallResults`, `Finish`
- Span: `ToolCalls` aggregate (child of LLM Call)
  - attrs: `total.count`, `by.name` (map name→count), `total.latency.ms`, `errors.count`
- Span: `ToolCall` per-call (child of ToolCalls)
  - attrs: `tool.name`, `args.schema.hash`, `args.size.bytes`, `confirmed` (для withConfirmation), `confirmation.id`
  - result: `result.size.bytes`
  - timing/status: `latency.ms`, `error.kind`, `error.message`
- Span: `VectorStore` (child of ToolCall или LLM Call, в зависимости от реализации)
  - attrs: `store.type`, `query.len`, `top_k`, `latency.ms`, `sources.meta` (id/score, опционально, с обфускацией)
- Span: `Handoff` (между агентами)
  - attrs: `from.agent`, `to.agent`, `reason`, `chain.depth`, `preserved.msgs.count`, `cycle_detected`

### Точки интеграции в activators/llm

1) `LLMActionContext` (ядро вызова LLM)
- Старт `LLM Call` span при начале запроса к провайдеру (в начале `startStream()`), запись входных атрибутов (model/props/messages/tools/memory и т.п.).
- Завершение `LLM Call` после восстановления `ChatCompletion` (в конце цикла `startStream()`/`chunkStream()`), фиксация finish_reason, токенов и latency; статус/ошибки.

2) `LLMActionContext` (streaming + tool loop)
- `startStream()` — старт `Streaming` span; фиксируем First-Byte Latency.
- `chunkStream()`/`eventStream()` — эмитим события (`ContentDelta`, `ToolCalls`, `ToolCallResults`), учитываем семплинг/редакцию.
- `withToolCalls {}` — старт aggregate `ToolCalls` span; внутри `callTools()` создаём per-call `ToolCall` span: аргументы (размер/хеш), статус, Latency, результат (размер), ошибки.

3) `LLMHooks` (наблюдаемость tool-вызовов)
- `LLMToolCallHook`, `LLMToolCallsHook` — связываем с текущим контекстом спанов, дублируем агрегаты в метрики.

4) Агентный уровень (`LLMAgent`, `LLMHandoff`)
- При входе агента — `Agent` span с атрибутами state/имени.
- В `handoff_to_agent` — `Handoff` span (child of текущего Agent), линкируем с новым `Agent` span.
- Цикл-guard события: `HandoffCyclePrevented`.

### Контекстная пропагация (в духе koog)
- Внутренний менеджер «живых» спанов (по аналогии с koog `SpanProcessor`):
  - потокобезопасный реестр активных доменных спанов (id→span), RW‑lock на update.
  - хранение `Context` у каждого доменного спана, `parent.context` → корректная иерархия.
  - утилиты: `getSpan<T>()`, форс‑закрытие висячих спанов по завершении сессии/ранa.
- Контекст переносится через:
  - `LLMActionContext` поля/корутины (parent span в coroutine context)
  - вызовы инструментов (корутины дочерние)
  - handoff (передача `Context` при `interrupt` / построении нового `Agent`)

### Метрики (рекомендуемый набор)
- Counters: `llm_calls_total`, `tool_calls_total`, `handoffs_total`, `errors_total{kind}`, `confirmations_total{status}`
- Histograms: `llm_latency_ms`, `first_byte_latency_ms`, `tool_call_latency_ms`, `vector_search_latency_ms`, `tokens_prompt`, `tokens_completion`, `tokens_total`
- Gauges: `active_streams`, `agent_chain_depth`
- Лейблы: `model`, `agent`, `tool`, `finish_reason`, `error_kind`, `store_type` (низкая кардинальность)

### Конфигурация и безопасность
- `LLMTelemetryFeature` (поверх `LLMAgentFeature`) и/или глобальная регистрация через `installTelemetryHooks(provider)`:
  - `samplingRate` (в т.ч. отдельный для streaming deltas)
  - `redactContent(policy|fn)` — full/partial/none, кастомные детекторы PII
  - `includeToolArgs` / `includeToolResults` (по имени инструмента)
  - `vectorStore.sourcePolicy` (id/score/обфускация)
- Значения по умолчанию: контент редактируется, аргументы/результаты инструментов не логируются, только размеры/хеши.

### Ошибки и ретраи
- Повторные попытки оформляем как siblings с link `retry_of` на первый `LLM Call`.
- Статусы: `OK`, `ERROR{kind}`, `UNSET` для принудительно закрытых.

### Интеграция с JAICF core
- Один `TelemetryProvider` на всё приложение.
- Регистрация LLM-инструментации при `BotEngine.withTelemetry(...)` через `installTelemetryHooks(provider)`; LLM-инструментация активируется внутри `LLMActionContext` и соответствующих LLM‑хуков.
- События/статусы совместимы с `core` (`TelemetryHookBinder`, `runWithTelemetry`).

### Тесты
- `activators/llm/src/testFixtures` использовать для перехвата `LLMToolCallHook`/`LLMToolCallsHook`.
- Проверять: иерархию спанов, атрибуты токенов/latency, события streaming, tool call ошибки/подтверждения, handoff links.

### Минимальный пример включения

```kotlin
val engine = BotEngine(
    scenario = MyScenario.model,
).withTelemetry(OpenTelemetryProvider(/* ... */))

val props = llmProps {
    model = "gpt-4o-mini"
    // Телеметрия активируется хук-установкой и точками в LLMActionContext (startStream/eventStream/withToolCalls)
}
```

Эта архитектура повторяет сильные стороны koog (lifecycle-менеджер спанов, строгая пропагация контекста и финализация) и нативно интегрируется в LLM-активатор JAICF через `LLMPipeline` и хуки, покрывая streaming, tool loop и handoff без изменения существующих сценариев.


