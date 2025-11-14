# Архитектура LLM-активатора JAICF

Документ описывает устройство `activators/llm` в `jaicf-kotlin-new`, поток обработки запросов и расширения (агенты, инструменты, векторные стора, тестовые утилиты). Материал основан на исходниках в `activators/llm/src/main/kotlin` и примерах `examples/llm-example`.

## 1. Общий поток обработки
- Состояния сценария подключают LLM через `llmState` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/scenario/LLMScenarioBuilder.kt:18`). Внутри создаётся `catchAll`‑активатор и вызывается `llmAction`.
- `llmAction` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/action/LLMAction.kt:35`) строит `LLMContext` и оборачивает user-блок в `LLMActionContext`, который управляет стримом, tool loop и реакциями. Исключения `LLMToolInterruptionException` перехватываются и выполняют отложенный callback (например, переход в другой state).
- `LLMActionAPI.createContext` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/action/LLMActionAPI.kt:31`) мержит дефолтные/внешние `LLMProps`, подставляет входные сообщения (через `LLMInputs.TextOnly` или кастомный builder) и учитывает сохранённые хенд-офф сообщения. Итоговый `ChatCompletionCreateParams` сразу дополняется специфическими свойствами OpenAI (tools, responseFormat, usage streaming).
- Во время выполнения `LLMActionContext.startStream()` открывает `StreamResponse` к OpenAI, аккумулирует чанки и восстанавливает `ChatCompletion` (методы `startStream`, `chunkStream`, `chatCompletion`, `content`, `toolCalls` в `activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/action/LLMActionContext.kt:64`).
- Перед каждым токеном выполняется `yield()` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/action/LLMActionContext.kt:70`), чтобы корректно реагировать на отмену корутины (например, при смене state).

## 2. Конфигурация через `LLMProps`
- `LLMProps` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/LLMProps.kt:30`) хранит параметры модели (model, temperature, топ‑p, токены и т.д.), список сообщений, функцию подготовки входа, инструменты и клиента OpenAI.
- Builder (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/LLMProps.kt:83`) доступен в DSL `LLMPropsBuilder`, где есть shortcuts `tool(...)`, `vectorStore(...)`, `llmMemory(...)`. `withProps` позволяет поверхностно комбинировать пресеты.
- `toChatCompletionCreateParams()` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/LLMProps.kt:63`) добавляет инструменты по их определениям. Для кастомных схем используется `JsonSchemaLocalValidation` и билд произвольного JSON Schema.
- `LLMInputs` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/LLMInputs.kt:12`) содержит готовые билдеры входа (`TextOnly`, `WithImages`), которые генерируют `ChatCompletionMessageParam` из `BotRequest`.
- `LLMMemory` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/LLMMemory.kt:12`) хранит историю в `BotContext.session`, поддерживает трансформации (например, `withSystemMessage`). При handoff и после tool call результаты возвращаются обратно в память.
- `LLMMessage` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/LLMMessage.kt:8`) концентрирует фабрики для разных ролей (user, assistant, system, tool) и безопасно сериализует результаты tool вызовов.

## 3. Цикл tool calls и события
- После запроса `LLMContext.withToolCalls` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/action/LLMActionContext.kt:136`) запускает цикл: получение tool вызовов (`hasToolCalls`), обработка коллбеков, отправка результата обратно в чат и повторный `startStream`. Возвращается, когда LLM перестаёт запрашивать инструменты.
- `callTools` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/action/LLMActionContext.kt:111`) параллелит выполнение инструментов, ошибки оборачивает в `Result`. Успешные вызовы превращаются в `LLMToolResult` и триггерят `LLMToolCallHook`/`LLMToolCallsHook` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/LLMHooks.kt:7`).
- `eventStream()` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/action/LLMActionContext.kt:156`) переводит поток чанков в `Stream<LLMEvent>`, позволяя прикладному коду реагировать на `Start`, `ContentDelta`, `ToolCalls`, `ToolCallResults`, `Finish`. Пример обработки показан в `examples/llm-example/src/main/kotlin/com/justai/jaicf/examples/llm/LLMEvents.kt`.
- В `reactions` доступны хелперы: `sayFinalContent()`, `streamOrSay()` для работы со streaming/финальным контентом (`LLMActionContext.kt:50` и `LLMActionContext.kt:167`).

## 4. Инструменты и подтверждение
- `LLMTool` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/tool/LLMTool.kt:19`) инкапсулирует определение (тип аргументов, имя, описание) и функцию. Аргументы десериализуются через Jackson mapper, что позволяет использовать обычные data классы.
- `LLMToolDefinition.CustomSchema` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/tool/LLMToolDefinition.kt:26`) создаёт OpenAI Function Tool c произвольной JSON Schema, удобно для динамических payload (см. `InlineTools` пример).
- `withConfirmation` оборачивает инструмент в `LLMToolWithConfirmation` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/tool/LLMToolWithConfirmation.kt:13`), который добавляет дополнительный параметр `confirmToolCallId`. Реализация поддерживает сценарии с подтверждением через LLM (текстовый ответ) или через собственный callback.
- Исключение `LLMToolInterruptionException` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/tool/LLMToolInterruptionException.kt:9`) позволяет временно прервать LLMAction и выполнить произвольный JAICF‑код (например, `Reactions.go(...)`).
- `LLMToolCallsHook` и `LLMToolCallHook` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/LLMHooks.kt:7`) дают срез данных о вызовах для логирования и тестов. Фикстура `LLMToolCallReaction` (`activators/llm/src/testFixtures/kotlin/com/justai/jaicf/activator/llm/LLMToolCallReaction.kt:6`) превращает hook в виртуальные реакции.
- HTTP shortcut'ы (в примерах `HTTPTools.kt`) используют обёртки из `com.justai.jaicf.activator.llm.tool.http.*` (непоказаны выше, но подключаются в props) и демонстрируют, что инструменты могут быть сформированы on-the-fly.

## 5. Векторные стора
- Любой `LLMVectorStore` реализует `search` и может быть прикрут по `vectorStore(...).tool(...)` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/vectorstore/LLMVectorStore.kt:19`). Возврат преобразуется в tool результат, а кастомный `responseBuilder` позволяет постобработать данные (пример: `ScenarioWithVectorStore.kt`).
- `OpenAIVectorStore` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/vectorstore/OpenAIVectorStore.kt:13`) оборачивает OpenAI Assistants API: поиск, загрузка файлов, список файлов. `OpenAIVectorStoreFactory` создаёт/удаляет стора.
- `HTTPVectorStore` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/vectorstore/HTTPVectorStore.kt:8`) — базовый класс для REST‑сторандов: создаёт `HttpClient` с Jackson и отключённой строгой десериализацией.

## 6. Агентный уровень
- `LLMAgent` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/agent/LLMAgent.kt:37`) — высокоуровневое описание одного состояния‑агента: хранит имя, props DSL, условие активации `onlyIf`, action блок. При первом обращении строит модель со state `/Agent/<name>` и подготавливает handoff props (`setupHandoffProps`).
- `withProps` (`agent/LLMAgent.kt:35`) позволяет модифицировать builder, например, выключить память (`withoutMemory`).
- `asBot` (`agent/LLMAgent.kt:130`) создаёт полноценный `BotEngine`, `asTool` (`agent/LLMAgent.kt:144`) превращает агента в инструмент (используется, например, для компоновки цепочек LLMов). Tool запускает вложенный `BotEngine`, собирает `SayReaction` и возвращает агрегированный ответ.
- `LLMAgentWithRole` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/agent/LLMAgentWithRole.kt:4`) дополнительно хранит роль; `LLMAgentWithGoal` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/agent/LLMAgentWithGoal.kt:9`) добавляет системные инструкции и auto-tool `goal_achieved` для контроля целей (тест `AgentWithGoalTest` проверяет цепочку).
- Хенд-оффы (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/agent/LLMHandoff.kt:20`) добавляют системное сообщение и инструмент `handoff_to_agent`, который через `interrupt` переключает диалог на другой агентский state, сохраняя цепочку сообщений в `BotContext.handoffMessages`. Проверка на циклы исключает зацикливание (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/agent/LLMHandoff.kt:59`). Пример — `MultiAgentHandoff`.

## 7. Память и handoff
- `LLMMemory` поддерживает две роли: `initial` (для чистого старта) и фактический список сообщений. Методы `transform`, `withSystemMessage`, `ifLLMMemory` позволяют безопасно менять историю (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/LLMMemory.kt:20`).
- При хенд-оффе не-системные сообщения сохраняются в `BotContext.handoffMessages` и подставляются следующему агенту (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/action/LLMActionAPI.kt:42`).

## 8. События и реакции
- Базовый набор событий описан в `LLMEvent` (`activators/llm/src/main/kotlin/com/justai/jaicf/activator/llm/LLMEvent.kt:7`) и используется как в `eventStream`, так и в тестах/пример. Это даёт возможность строить произвольные пайплайны отображения: например, `AgentWithTools` выводит промежуточные результаты, а `ScenarioWithStreaming` создаёт «CALLING / RESULTS» лог.
- Реакции: помимо стандартных `say`, доступны `reactions.stream(...)` (через `Reactions.stream` extension) и `reactions.handoff` (`LLMHandoff.kt:83`) для самих handoff функций.

## 9. Тестовая инфраструктура
- Аннотация `@OpenAITest` (`activators/llm/src/testFixtures/kotlin/com/justai/jaicf/activator/llm/openai/OpenAITest.kt:8`) подключает `OpenAIExtension`, которая проверяет `OPENAI_API_KEY` и повторяет тесты при флаky‑ответах (`activators/llm/src/testFixtures/kotlin/com/justai/jaicf/activator/llm/openai/OpenAIExtension.kt:12`).
- `LLMScenarioTest` (`activators/llm/src/testFixtures/kotlin/com/justai/jaicf/activator/llm/LLMScenarioTest.kt:8`) наследует `ScenarioTest`, перехватывает `LLMToolCallsHook`, добавляет DSL `callsTool`, `callsTools`, `withoutToolCalls` — см. `AgentWithToolsTest`.
- `testWithLLM` (`activators/llm/src/testFixtures/kotlin/com/justai/jaicf/activator/llm/LLMTest.kt:52`) запускает отдельный «тестирующий» агент, который общается с target ботом и ожидает структурированный JSON‑ответ (`LLMTestResult`). Это позволяет формулировать требования на естественном языке и проверять их (пример: `SimpleLLMAgentTest`).

## 10. Примеры использования
- **Базовый state** — `ScenarioWithTools.kt` показывает, как `llmState` + `llmProps` создают чат с калькулятором и памятью.
- **Streaming и tool loop** — `ScenarioWithStreaming.kt` и `AgentWithTools` реализуют `llm.withToolCalls {}` и логирование этапов.
- **Подтверждение** — `ConfirmToolCall.kt` использует `.withConfirmation`, тест `ConfirmToolCallTest` проверяет повторный вызов только после подтверждения.
- **Vector store** — `ScenarioWithVectorStore.kt` комбинирует OpenAI Vector Store с кастомным `responseBuilder`, собирая и выводя источники.
- **Мультиагентность** — `MultiAgentHandoff.kt` строит два агента и связывает handoff цепью. Тест `MultiAgentHandoffTest` убеждается, что запросы уходят к нужному агенту.

## 11. Ключевые точки расширения
1. `LLMPropsBuilder` — точка настройки модели, инструментов, памяти и клиента.
2. `LLMActionBlock`/`LLMActionContext` — управление стримом, доступ к `eventStream`, `withToolCalls`, `awaitStructuredContent`.
3. `LLMTool` API — подключение кастомных функций, HTTP/VectorStore обёрток, подтверждений.
4. `LLMAgent` — композиция LLM‑состояний, превращение в бота/инструмент, handoff цепочки.
5. Хуки (`LLMToolCallHook`, `LLMToolCallsHook`) — наблюдаемость и тесты.

## 12. Телеметрия (интеграция без LLMPipeline)
- Подключение: через `BotEngine.withTelemetry(...)` (используется core‑`TelemetryProvider` и `installTelemetryHooks`). Дополнительно может быть опциональный `LLMTelemetryFeature`.
- Точки инструментации в activators/llm:
  - `LLMActionContext.startStream()` — старт `LLM Call` span и `Streaming` span, фиксация First‑Byte Latency, входных атрибутов (model/props/tools/memory и т.п.).
  - `LLMActionContext.chunkStream()` / `eventStream()` — события стрима (`Start`, `ContentDelta` с редактированием/семплингом, `ToolCalls`, `ToolCallResults`, `Finish`).
  - `LLMActionContext.withToolCalls {}` / `callTools()` — aggregate `ToolCalls` span и per‑call `ToolCall` спаны (latency, статус, размеры аргументов/результатов, ошибки; подтверждения для `.withConfirmation`).
  - Vector Store вызовы — отдельный `VectorStore` span (тип стора, topK, latency, источники с обфускацией при необходимости).
  - Агентный уровень (`LLMAgent`) — `Agent` span на входе; `LLMHandoff` — `Handoff` span, линки между агентами, цикл‑guard событие.
- Контент и приватность: по умолчанию контент редактируется; аргументы/результаты инструментов не логируются целиком (только размеры/хеши). Семплинг для `ContentDelta`.
- Метрики: счётчики (llm/tool/handoff/errors), гистограммы (latency, tokens, FBL), gauge (active_streams, agent_chain_depth); лейблы низкой кардинальности (model, agent, tool, finish_reason, error_kind, store_type).

Эти компоненты формируют гибкий слой между JAICF-сценариями и OpenAI Chat Completions, позволяя строить stateful-agents, orchestrate tool calls и интегрировать внешние знания без переписывания основного движка.
