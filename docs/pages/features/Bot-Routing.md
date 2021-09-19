---
layout: default
title: Bot Routing
permalink: Bot-Routing
parent: Features
---

**Bot Routing** feature enables developers to use many bots inside one channel. One logical scenario can be separated
into several independent bots with it own NLU providers or business logic. These are some example usages:

* Multilingual Bots with separate NLU
* A/B Testing
* Canary Deployment

### BotRoutingEngine

`BotRoutingEngine` is an implementation of `BotEngine` allowing developers to change executable `BotEngine` for
different clients.

```kotlin
val MainBotEngine = BotEngine(MainScenario)

val MultilingualBotEngine = BotRoutingEngine(
    main = "main" to MainBotEngine,
    routables = mapOf("firstEngine" to FirstExampleEngine, "secondEngine" to SecondExampleEngine)
)
```

* parameter `main` defines a default engine to process requests until client is routed to another engine.
* parameter `routables` defines a list of routable engine client requests can be routed to.

When client is routed to another engine, he or she will remain using specified engine until routing back or forward to
next bot.
> Using `Bot Routing` requires a shared instance of `BotContextManager` for every routable engine as `BotRoutingContext`
> is stored inside client context. Cleaning client context means erasing all routing state, the execution will be passed back to `main` engine.

### BotRoutingApi

`BotRoutingApi` can be accessed from `action` blocks inside scenario. This API grants access to following methods:

* `route` - route current bot request to specified engine with name from `routables` map. Next requests will be also
  send to this engine.
* `routeBack` - route current bot request back to previous bot engine
* `changeEngine` - route client all next requests to specified engine with name from `routables` map.
* `changeEngineBack` - route client all next requests back to previous bot engine.

### Example

Example usage of **Bot Routing** can be found
at [Multilingual Bot Example](https://github.com/just-ai/jaicf-kotlin/tree/master/examples/multilingual-bot).




