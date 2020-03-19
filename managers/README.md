Managers are used by JAICF to persist and retrieve a [BotContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/context/BotContext.kt) instances that contain a dialogue state and user-related arbitrary data.

# Supported managers list

* [MongoDB](https://github.com/just-ai/jaicf-kotlin/tree/master/managers/mongo)
* [MapDB](https://github.com/just-ai/jaicf-kotlin/tree/master/managers/mapdb)

# How to use

Please refer to the appropriate manager's manual to learn how to use it in your JAICF project.
In general all you need to do - is to add a corresponding dependencies in your _build.gradle_ file and add a manager instance to the agent's configuration.

JAICF stores and retrieves context data classes transparently thus you don't have to do any special things in your scenarios.

> Learn more about context [here](https://github.com/just-ai/jaicf-kotlin/wiki/context).