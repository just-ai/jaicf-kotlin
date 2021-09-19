In this quick start guide, you will create a JAICF project from an existing template and then we'll go briefly through its sources.

## 1. Create a new project

To create a new project you have to have some appropriate IDE installed. We recommend using [IntelliJ IDEA](https://www.jetbrains.com/idea/) due to its native support of [Kotlin language](https://kotlinlang.org/).

![Template project](https://i.imgur.com/NWOoEY2.png)

Create a new project using **File > New > Project from version Control** from the menu.
Paste this template's URL [https://github.com/just-ai/jaicf-template](https://github.com/just-ai/jaicf-template) and click **Clone**.

That is all! This simple project implements a voice application for the Google Actions platform.
In the next steps, we have to create a Dialogflow agent and connect it with our dialogue scenario.

## 2. Run the project

To run our project you have only to click on the green play button in _Server.kt_ file.

![Start the project](https://i.imgur.com/HDFdy6t.gif)

This will start a local server on port 8080. We have now to make it public for the entire Internet.

The easiest way to do this is to install [ngrok](https://ngrok.com) and start `ngrok http 8080` in the terminal.
This generates a temporal public URL that can be used to configure a Fulfilment URL in the Dialogflow console.
Just copy it and go to the final steps.

## 3. Create a Dialogflow agent

Go to [dialogflow.com](https://dialogflow.com), sign in, and create a new agent.
Here you have to open Fulfilment settings, enable the fulfillment feature, and paste your URL. Click **Save** then.

![](https://i.imgur.com/TiPQVlm.png)

Then go to two existing intents and enable fulfillment for each at the bottom of the page. Click **Save** for each intent.

## 4. Test it!

Great! You have created a new JAICF project and connected it with the Dialogflow agent.
Now it is time to test how it works.

Just click on **Integrations** -> **Google Assistant** link on the left sidebar and then click **Test** button in a pop-up window. Click the **Talk to my test app** button in the Assistant emulator then.

This launches a conversation between Google Assistant and your JAICF project.

_Template project doesn't implement much interesting dialogue scenario thus it ends the session on every user's phrase that doesn't match greeting intent._

# A bit of explanations

Now we are going to go through the project's source briefly to understand in general how it works.

## Dependencies

Let's look on the `build.gradle.kts` file. Here a JAICF libraries from `jcenter` repository are defined in the `dependencies` section:

```kotlin
repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.just-ai.jaicf:core:$jaicf")
    implementation("com.just-ai.jaicf:google-actions:$jaicf")
    implementation("com.just-ai.jaicf:mongo:$jaicf")

    implementation("io.ktor:ktor-server-netty:$ktor")
}
```

> Learn more about JAICF installing options [here](Installing).

## Scenario

Let's look at the `MainScenario` source file.

```kotlin
val MainScenario = Scenario {
    state("main") {
        activators {
            intent(DialogflowIntent.WELCOME)
        }

        action {
            reactions.say("Hi there!")
        }
    }

    fallback {
        reactions.say("I have nothing to say yet...")
        reactions.actions?.run {
            say("Bye bye!")
            endConversation()
        }
    }
}
```

JAICF scenario contains a set of dialogue _states_ that can be nested in each other.
Thus a context-aware dialogue with a hierarchy of states could be described via JAICF DSL.
Every state could be _activated_ by different activators like Intent that is recognized by the NLU engine from the user's raw query.

Here is a `MainScenario` object, obtained by calling the `Scenario` function, that contains definitions of the two states named _main_ and _fallback_.
The main state can be activated by `WELCOME` intent of the Dialogflow NLU engine. The second state named _fallback_ is activated each time the user speaks something that is not handled by any other activators of any state. That is why it is named `catchAll()` activator.

Once the user speaks something that is recognized as a `WELCOME` intent (like "hi", "hello there", etc.), JAICF activates a corresponding state _main_ and executes its _action_ block. The same regarding the _fallback_ state.

This scenario responds with a simple "Hi there!" string once the _main_ state is activated and "I have nothing to say yet..." on _fallback_.

You can see how the _fallback_ action reacts when the request is going from the Google Actions channel.

```kotlin
actions {
    reactions.say("Bye bye!")
    reactions.endConversation()
}
```

Here is a [Kotlin extensions](https://kotlinlang.org/docs/reference/extensions.html) power activated!
When the user says something that is not handled by other states (fallback), your scenario responds with the same "I have nothing to say yet..." but adds one more reply - "Bye bye!" and ends a conversation.
`endConversation()` is a channel-related method of the Google Actions channel library that switches off the Google Assistant microphone and exists from your Action.

Here you can see how it is easy to create conversational scenarios that can work simultaneously via different channels but has logic forks for some of them.

> Learn more about channels [here](Channels).

## Bot configuration

Obviously, every scenario has to run to handle users' requests.
To do that you have to instantiate a new `BotEngine` that holds your scenario and connects to the desired NLU engine.

```kotlin
val templateBot = BotEngine(
    scenario = MainScenario,
    activators = arrayOf(
        ActionsDialogflowActivator,
        CatchAllActivator
    )
)
```

Here is a `templateBot` configuration that holds the `MainScenario` and configures desired activators - `ActionsDialogflowActivator` and `CatchAllActivator`.

> Learn more about different activators [here](Natural-Language-Understanding).

## HTTP Server

To run this bot we have to start an HTTP server once Google Actions requires us to provide a webhook endpoint.
JAICF already provides a ready-to-use Ktor extension that helps to use the Ktor HTTP server with ease.

```kotlin
fun main() {
    embeddedServer(Netty, 8080) {
        routing {
            httpBotRouting(
                "/" to ActionsFulfillment.dialogflow(templateBot)
            )
        }
    }.start(wait = true)
}
```

Here we start Netty HTTP server on port 8080 and provide routing that proxies all requests to our bot.

# Where to go next

In this quick start guide, you've learned how to create a new JAICF project and simple dialogue scenario, as well as how to start a webhook server for the Google Actions channel and start to test your dialogue via the Dialogflow console.

Of course, JAICF enables you to create much more powerful and flexible things like 

* [cross-platform support](Channels)
* [managing a dialogue state](Scenario-DSL)
* [automatic testing](Testing)
* and much more

To dive into the JAICF, we recommend going next through the [Introduction section](Introduction) and check for [some JAICF project examples](https://github.com/just-ai/jaicf-kotlin/tree/master/examples).