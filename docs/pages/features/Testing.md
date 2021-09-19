---
layout: default
title: Testing
permalink: Testing
parent: Features
---

JAICF provides a [JUnit-based](https://junit.org/junit5/) test suite that can be used to test your scenarios and agents automatically on each build.

# Benefits of automatic tests

Dialogue agents are complex context-aware applications.
Depending on the size and complexity of the dialogue scenarios in your conversational agent, the manual testing of each branch of such agent could be very difficult and annoying.

_As a result, the coverage of such manual testing may be very low._

JAICF solves this problem providing a [JUnit-based](https://junit.org/junit5/) test suite to enable developer write an automatic tests for JAICF agents.
Running on each gradle build, automatic tests guarantee that your conversational project works properly with each change you commit.

# Example

```kotlin
class HelloWorldScenarioTest: ScenarioTest(HelloWorldScenario) {

    @Test
    fun `Asks a name of each new user`() {
        query("hi") endsWithState "/helper/ask4name"
    }

    @Test
    fun `Greets a known user`() {
        withBotContext { client["name"] = "some name" }
        query("hi") endsWithState "/main"
    }
}
```

As you can see, this is a known JUnit test with helper extensions for conversational-specific assertions.

## More examples

Please look at [example projects](https://github.com/just-ai/jaicf-kotlin/tree/master/examples) to learn how JAICF test suite is used on practice.

# How to use

You can refer to the [BotTest](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/test/BotTest.kt) and [ScenarioTest](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/test/ScenarioTest.kt) classes that should be inherited for each bot or scenario test.
These classes contain a helper functions to write an automatic dialogue tests.

Also you have to append a JUnit dependencies and `useJUnitPlatform` directive to your _build.gradle_:

```kotlin
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
```

# runInTest block

There is a special `runInTest` block that is invokes only if scenario is running in test mode.
This block can be used inside the `action` block of any state:

```kotlin
action {
    var gamers: Int? = null
    
    ...

    runInTest {
        gamers = getVar("gamers") as? Int
    }
}
```

This block is running with [TestActionContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/test/context/TestActionContext.kt) that contains additional helper functions that can be used in test mode.

# Random

JAICF contains special helper `random` function that returns a random element from the list.
This is helpful for such cases as responding with a random text reply to the user.

When running in test mode, this method always returns a first element.
A helper `sayRandom` method always replies with the first text reply.