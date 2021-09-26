---
layout: default
title: Mongo DB
permalink: Mongo-DB
parent: Environments
---

![](/assets/images/env/mongodb.png)

Allows to use [Mongo database](https://www.mongodb.com/) to persist [BotContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/context/BotContext.kt) instances.

> Learn more about context [here](https://github.com/just-ai/jaicf-kotlin/wiki/context).

# How to use

#### 1. Include Mongo DB dependency to your _build.gradle_

```kotlin
implementation("com.just-ai.jaicf:mongo:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

#### 2. Create a Mongo database

You can install and run Mongo DB locally or use any cloud-based mongo hosting like [Atlas](https://www.mongodb.com/cloud/atlas).

#### 3. Obtain Mondo DB URL

When you've created a Mongo DB, you can obtain its connection URL that should be used on the next step.

#### 4. Configure Mongo DB manager

```kotlin
var client = MongoClients.create("Your Mongo Connection String")
val manager = MongoBotContextManager(client.getDatabase("jaicf").getCollection("contexts"))

val templateBot = BotEngine(
    scenario = MainScenario,
    defaultContextManager = manager,
    activators = arrayOf(
        AlexaActivator
    )
)
```
