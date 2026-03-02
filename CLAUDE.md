# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

JAICF (Just AI Conversational Framework) is a Kotlin-based framework for building conversational voice assistants and chatbots. It provides a DSL for writing dialogue scenarios that can connect to multiple channels (Telegram, Alexa, Facebook, etc.) and NLU engines (Dialogflow, CAILA, Rasa).

## Build Commands

```bash
# Build entire project
./gradlew build

# Run all tests
./gradlew test

# Build specific module
./gradlew :core:build
./gradlew :channels:telegram:build
./gradlew :activators:dialogflow:build

# Run tests for specific module
./gradlew :core:test
./gradlew :examples:hello-world:test
```

## Project Structure

```
jaicf-kotlin/
├── core/                    # Core framework - DSL, BotEngine, test API
├── channels/                # Channel implementations (telegram, alexa, slack, etc.)
├── activators/              # NLU engine integrations (dialogflow, caila, rasa, lex)
├── managers/                # Context persistence (mongo, mapdb)
├── examples/                # Example bot projects
├── buildSrc/                # Gradle build plugins and dependencies
└── gradle-plugins/          # Additional Gradle plugins
```

## Architecture

### Core Components

1. **Scenario** (`core/src/main/kotlin/com/justai/jaicf/builder/`): DSL entry point for defining bot dialogue flows
   - `Scenario {}` - Creates a scenario
   - `state()` - Defines a dialogue state
   - `activators {}` - Defines what triggers a state (intents, events, regex, catchAll)
   - `action {}` - Code executed when state is activated

2. **BotEngine** (`core/src/main/kotlin/com/justai/jaicf/BotEngine.kt`): Main bot processor
   - Processes requests through activators
   - Manages dialogue context
   - Supports hooks for request lifecycle

3. **Activators** (`core/src/main/kotlin/com/justai/jaicf/activator/`): Match user input to states
   - Built-in: `RegexActivator`, `CatchAllActivator`, `BaseEventActivator`, `BaseIntentActivator`
   - External: `DialogflowIntentActivator`, `CailaIntentActivator`, `RasaIntentActivator`

4. **Channels** (`channels/*/`): Platform-specific integrations
   - Each channel provides: `BotRequest`, `Reactions`, `BotChannel` implementations
   - Examples: `TelegramChannel`, `AlexaChannel`, `FacebookChannel`

5. **Context** (`core/src/main/kotlin/com/justai/jaicf/context/`):
   - `BotContext` - User session data with `client`, `session`, `temp` storage
   - `ActionContext` - Available in action blocks with `context`, `activator`, `request`, `reactions`
   - `DialogContext` - Tracks current state, transitions, back state stack

6. **Reactions** (`core/src/main/kotlin/com/justai/jaicf/reactions/Reactions.kt`): Response API
   - `say()`, `sayRandom()`, `image()`, `buttons()`, `audio()`
   - `go()`, `changeState()` - Navigate between states
   - `goBack()`, `changeStateBack()` - Return to callback state

### Scenario DSL Pattern

```kotlin
val MyScenario = Scenario {
    state("stateName") {
        activators {
            intent("intentName")
            regex("pattern")
            event("eventName")
            catchAll()
        }
        action {
            // context: BotContext, activator: ActivatorContext
            // request: BotRequest, reactions: Reactions
            reactions.say("Hello!")
            reactions.go("/nextState")
        }

        state("nestedState") { /* ... */ }
    }

    append(OtherScenario)  // Merge scenarios
    append("context", OtherScenario, modal = true)  // As sub-scenario
}
```

### Channel-Specific Actions

Use channel type tokens for platform-specific code:

```kotlin
action {
    telegram {
        reactions.say("Telegram-specific: ${request.message.chat.firstName}")
    }
    alexa {
        reactions.endSession("Goodbye!")
    }
}
```

### Activator-Specific Actions

```kotlin
action(dialogflow) {
    val slot = activator.slots["slotName"]
}
```

## Testing

Extend `ScenarioTest` or `BotTest` for testing scenarios:

```kotlin
class MyScenarioTest : ScenarioTest(MyScenario) {
    @Test
    fun `test state transition`() {
        query("hello") endsWithState "/greeting"
    }

    @Test
    fun `test with context`() {
        withBotContext { client["name"] = "John" }
        withCurrentContext("/main")
        query("hi") responds "Hello John!"
    }
}
```

Key test methods:
- `query(text)`, `intent(name)`, `event(name)` - Send requests
- `endsWithState`, `goesToState`, `responds` - Assert results
- `withBotContext {}`, `withCurrentContext()`, `withBackState()` - Setup context

## Dependencies (buildSrc)

Version constants in `buildSrc/src/main/kotlin/Version.kt`:
- Kotlin: 1.4.21
- Ktor: 1.5.1
- JUnit: 5.6.0

Gradle plugins in `buildSrc/src/main/kotlin/Plugins.kt`:
- `jaicf-kotlin` - Standard Kotlin setup
- `jaicf-publish` - Maven Central publishing
- `jaicf-junit` - Test configuration

## Module Dependencies

- All channel/activator modules depend on `core` via `core()` function
- Use `api()` for transitive dependencies exposed to consumers
- Use `implementation()` for internal dependencies