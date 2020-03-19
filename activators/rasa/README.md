<p align="center">
    <img src="https://rasa.com/static/rasa-logo-60e441f8eadef13bea0cc790c8cf188b.svg" height="128"/>
</p>

<h1 align="center">Rasa NLU activator</h1>

Allows to use [Rasa AI](https://rasa.com) NLU engine as a states activator in JAICF.

## How to use

#### 1. Include Rasa dependency to your _build.gradle_

```kotlin
implementation("com.justai.jaicf:rasa:$jaicfVersion")
```

#### 2. Use Rasa `activator` in your scenario actions

```kotlin
state("launch") {
    activators {
        intent("greet")
    }

    action {
        // Recognised named entities
        val slots = activator.rasa?.slots
    }
}
```

#### 3. Create and run Rasa NLU

Create your NLU model that includes intents and named entities you need.
There is a [great tutorial](https://rasa.com/docs/rasa/user-guide/rasa-tutorial/) that shows how to install and run Rasa NLU models.

Then you have to [train and run NLU only server](https://rasa.com/docs/rasa/nlu/using-nlu-only/) and obtain it's public URL (using [ngrok](https://ngrok.com) for example).

#### Rasa NLU server template

We provide a ready to use [Rasa NLU server template](https://github.com/just-ai/rasa-heroku-template) that can be ran on the Heroku cloud with one click only.
This simplifies the process of Rasa NLU server installation and provides a way to update NLU model as well.

#### 4. Configure Rasa activator

Then you have to provide your Rasa public URL in the activator's configuration:

```kotlin
val helloWorldBot = BotEngine(
    model = HelloWorldScenario.model,
    activators = arrayOf(
        RasaIntentActivator.Factory(RasaApi("https://your-rasa-server.com"))
    )
)
```