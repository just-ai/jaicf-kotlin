<p align="center">
    <img src="https://just-ai.com/wp-content/themes/justai/img/caila_sign.svg" height="128" width="128"/>
</p>

<h1 align="center">Caila NLU activator</h1>

Allows to use [Caila](https://just-ai.com/en/caila-conversational-ai-linguistic-assistant.php) NLU engine as a states activator in JAICF with named entity recognition.

Basic template with Caila activator can be found [here](https://github.com/just-ai/jaicf-jaicp-caila-template).

## How to use

#### 1. Include Caila dependency to your _build.gradle_

```kotlin
implementation("com.just-ai.jaicf:caila:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

#### 2. Use Caila `activator` in your scenario actions

```kotlin
state("launch") {
    activators {
        intent("Hello")
    }

    action {
        // Fills slots in intent
        val slots = activator.caila?.slots

        // Recognizes entities in query
        val entities = activator.caila?.entities

        // May contain answer 
        activator.caila?.topIntent?.answer?.let {
            reactions.say(it)
        }

        // contains top N inference variants 
        val variants = activator.caila?.result?.inference?.variants
        
        // contains phrase markup (tokenization, lemmatization)
        val markup = activator.caila?.result?.markup
    }
}
```

> Learn more about [CailaIntentActivatorContext](https://github.com/just-ai/jaicf-kotlin/blob/master/activators/caila/src/main/kotlin/com/justai/jaicf/activator/caila/CailaIntentActivatorContext.kt).

#### 3. Create project in [JAICP Application Panel](https://app.jaicp.com/register?utm_source=github&utm_medium=article&utm_campaign=quickstart)

All you need to use Caila is to create project in [JAICP Application Panel](https://app.jaicp.com/register?utm_source=github&utm_medium=article&utm_campaign=quickstart).
We have full guide, [How to integrate with JAICP](https://github.com/just-ai/jaicf-kotlin/wiki/Quick-Start-With-JAICP), but, in general, you have to:
1. Register [here](https://app.jaicp.com/register?utm_source=github&utm_medium=article&utm_campaign=quickstart),
2. Create JAICF Project,
3. Copy and paste token.

#### 4. Configure Caila activator

```kotlin
val cailaActivator = CailaIntentActivator.Factory(
    CailaNLUSettings(
        accessToken = "<your_jaicp_access_token>", 
        confidenceThreshold = 0.2,
        // optional thresholds for patterns and phrases match. If it's not specified, confidenceThreshold will be used
        intentThresholds = IntentThresholds (
            patterns = 0.3,
            phrases = 0.3
        )
))

val helloWorldBot = BotEngine(
    scenario = HelloWorldScenario,
    activators = arrayOf(
        cailaActivator
    )
)
```
