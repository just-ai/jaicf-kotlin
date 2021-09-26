---
layout: default
title: JAICP Cloud
permalink: JAICP-Cloud
parent: Environments
nav_exclude: true
---

<p align="center">
<img src="/assets/images/env/jaicp.svg" width=200>
</p>

<h1 align="center">JAICP Cloud</h1>

[JAICP](https://app.jaicp.com) (Just AI Conversational Platform) provides a cloud hosting to serve JAICF projects.
This makes it much easier to use this solution to automatically build and deploy JAICF project from source codes.

# Example

Here is a [ready to use template](https://github.com/just-ai/jaicf-jaicp-caila-template) that can be deployed to JAICP Cloud.
Please investigate its `build.gradle.kts` and `JaicpServer.kt` to learn how to configure your JAICF project to make it compatible with JAICP Cloud.

# How to use

There are simple steps you have to make to deploy your JAICF project to JAICP Cloud.

### 0. Add JAICP dependencies

Add following dependencies in your project's _build.gradle.kts_

```kotlin
dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("ch.qos.logback:logback-classic:$logback")

    implementation("com.just-ai.jaicf:core:$jaicf")
    implementation("com.just-ai.jaicf:jaicp:$jaicf")
    implementation("com.just-ai.jaicf:caila:$jaicf")
}
```

Replace `$jaicf` with the latest version of JAICF.

### 1. Create main class

JAICP expects your JAICF project to be ran in special way using `JaicpServer`.
Thus you have to create main class that utilises this class the next way:

```kotlin
fun main() {
    JaicpServer(
        botApi = templateBot,
        accessToken = accessToken,  // JAICP project API token
        channels = listOf(          // List of configured JAICP channels
            ChatApiChannel,
            ChatWidgetChannel,
            TelephonyChannel
        )
    ).start(wait = true)
}
```

### 2. Add JAICP build plugin

JAICP builds your JAICF project automatically.
To make it possible you have to append a special plugin to your _build.gradle.kts_:

```kotlin
plugins {
    application
    kotlin("jvm") version "1.3.71"
    id("com.justai.jaicf.jaicp-build-plugin") version "0.1.1"
}
```

And configure main class that utilises `JaicpServer`:

```kotlin
application {
    mainClassName = "com.justai.jaicf.template.connections.JaicpServerKt"
}

tasks.withType<com.justai.jaicf.plugins.jaicp.build.JaicpBuild> {
    mainClassName.set(application.mainClassName)
}
```

> Learn more about JAICP build plugin [here](https://github.com/just-ai/jaicf-kotlin/tree/master/gradle-plugins/jaicp-build-plugin)

### 3. Push your JAICF project to Git repository

JAICP fetches projects only from any Git repositories like [Github](https://github.com).
Thus you have to push your project's sources to some Git repository before you can deploy it to JAICP.

### 4. Create JAICF project on JAICP

In your [JAICP Application Panel](https://app.jaicp.com/register?utm_source=github&utm_medium=article&utm_campaign=quickstart) create a new JAICF project with **JAICP Cloud** in _Runtime environment_ section and **Connect to existing project** in _Initial code_ section.
Then paste your JAICF project's Git repository URL on the **Location** tab and click **Create**.

### 5. Append channels

On the _Channels_ panel add required channels.

> Note that you have to append only those types of channels that are presented in your `JaicpServer` configuration.

Once the very first channel is created, JAICP automatically starts to build and deploy your project.

### 6. Push changes to source codes

Once you're ready with some local code changes, you just have to push it to the same Git repository - JAICP automatically fetches and deploys these changes to the Cloud.

# JAICP button

JAICP provides a HTML button that can be used to deploy any JAICF project to the JAICP Cloud in a single click.

[![Deploy](https://just-ai.com/img/deploy-to-jaicp.svg)](https://app.jaicp.com/deploy?template=https://github.com/just-ai/jaicf-jaicp-caila-template)

### How to use

You can place it on any HTML page or on `README.md` file of your source codes on Github.
Once the user clicks this button, JAICP automatically deploys a source code that is linked to this button.

To place this button on your Github project's `README.md` just add

```markdown
[![Deploy](https://just-ai.com/img/deploy-to-jaicp.svg)](https://app.jaicp.com/deploy)
```

For any third-party HTML page it should be changed to

```html
<a href="https://app.jaicp.com/deploy?template=https://github.com/just-ai/jaicf-jaicp-caila-template"><img src="https://just-ai.com/img/deploy-to-jaicp.svg"></a>
```

_Just replace `template` parameter with desired Github repository link to deploy it to JAICP._

### CAILA NLU model import

If your project requires some special CAILA NLU model with configured intents, entities and settings, you may place a `caila_import.json` file to the root of your Github project.
JAICP automatically fetches this file on the first deploy and initialises CAILA with its content.

> Please take a look on the [caila_import.json example](https://github.com/just-ai/jaicf-jaicp-caila-template/blob/master/caila_import.json)

# Analytics and Logs

JAICP transparently stores every user's request to your bot and responses returned from it.
To make it possible you have to append some special conversation loggers to your `BotEngine` configuration:

```kotlin
val templateBot = BotEngine(
    model = MainScenario.model,
    conversationLoggers = arrayOf(
        JaicpConversationLogger(accessToken),
        Slf4jConversationLogger()
    ),
    activators = arrayOf(
        CailaIntentActivator.Factory(cailaNLUSettings),
        RegexActivator
    )
)
```

## Logs
You can find real-time logs generated by your bot at **Logs tab** (bottom panel).

## Dialogs
There are also dialogs between users and the bot available on the **Analytics** -> **Dialogs** of the left panel.

> You can find it very useful to append unresolved phrases to new or existing CAILA intents using **Phrases tab**.
This speed-ups the daily intents improvement routine.

You can learn more about Dialogs feature [here](https://help.just-ai.com/#/docs/en/analytics/dialog_logs).

# Persistence

JAICP Cloud doesn't provide any built-in persistence layer to store your bot's [context](context).
Thus you have to use some third-party solutions like [Mongo DB](https://github.com/just-ai/jaicf-kotlin/tree/master/managers/mongo).

# Environment variables

JAICP Cloud automatically propagates `JAICP_API_TOKEN` environment variable to your JAICF instance that contains a value of the current JAICP project API token. Thus you don't have to save this token in your source code.