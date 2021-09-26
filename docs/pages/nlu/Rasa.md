---
layout: default
title: Rasa
permalink: Rasa
parent: Natural Language Understanding
---

<p align="center">
    <img src="/assets/images/nlu/rasa.svg" width="200"/>
</p>

<h1 align="center">Rasa NLU activator</h1>

Allows to use [Rasa AI](https://rasa.com) NLU engine as a states activator in JAICF.

## How to use

### NLU server template

We provide a ready to use [Rasa NLU server template](https://github.com/just-ai/rasa-heroku-template) that can be ran on the [Heroku cloud](https://heroku.com) with a single click.
This simplifies the process of Rasa NLU server installation and provides a way to update NLU model as well.

### Guide

#### 1. Include Rasa dependency to your _build.gradle_

```kotlin
implementation("com.just-ai.jaicf:rasa:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

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

Then you have to [train and run NLU only server](https://rasa.com/docs/rasa/nlu/using-nlu-only/) and obtain its public URL (using [ngrok](https://ngrok.com) for example).

#### 4. Configure Rasa activator

Then you have to provide your Rasa public URL in the activator's configuration:

```kotlin
val helloWorldBot = BotEngine(
    scenario = HelloWorldScenario,
    activators = arrayOf(
        RasaIntentActivator.Factory(RasaApi("https://your-rasa-server.com"))
    )
)
```
