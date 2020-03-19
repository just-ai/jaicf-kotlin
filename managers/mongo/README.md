![](https://upload.wikimedia.org/wikipedia/commons/thumb/9/93/MongoDB_Logo.svg/1280px-MongoDB_Logo.svg.png)

Allows to use [Mongo database](https://www.mongodb.com/) to persist [BotContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/context/BotContext.kt) instances.

> Learn more about context [here](https://github.com/just-ai/jaicf-kotlin/wiki/context).

# How to use

#### 1. Include Mongo DB dependency to your _build.gradle_

```kotlin
implementation("com.justai.jaicf:mongo:$jaicfVersion")
```

#### 2. Create a Mongo database

You can install and run Mongo DB locally or use any cloud-based mongo hosting like [Atlas](https://www.mongodb.com/cloud/atlas).

#### 3. Obtain Mondo DB URL

When you've created a Mongo DB, you can obtain its connection URL that should be used on the next step.

#### 4. Configure Mongo DB manager

```kotlin
val uri = MongoClientURI("Mongo DB URL with credentials")
val client = MongoClient(uri)

val manager = MongoBotContextManager(
    client.getDatabase(uri.database!!).getCollection("contexts") // or any other collection
)

val templateBot = BotEngine(
    model = MainScenario.model,
    contextManager = manager,
    activators = arrayOf(
        ActionsDialogflowActivator,
        CatchAllActivator
    )
)
```