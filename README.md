![](https://i.imgur.com/ONpTipu.png)

JAICF is a comprehensive enterprise-level framework from [Just AI](https://just-ai.com) for conversational voice assistants and chat bots development using Kotlin-based DSL.

<br/>

[![Build Status](https://travis-ci.org/just-ai/jaicf-kotlin.svg?branch=master)](https://travis-ci.org/just-ai/jaicf-kotlin)
[![Download](https://api.bintray.com/packages/just-ai/jaicf/core/images/download.svg) ](https://bintray.com/just-ai/jaicf/core/_latestVersion)
[![GitHub License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://github.com/just-ai/jaicf-kotlin/blob/master/LICENSE)

```kotlin
repositories {
    mavenCentral()
    jcenter()
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.justai.jaicf:core:$jaicfVersion")
}
```
```kotlin
object HelloWorldScenario: Scenario() {
    init {
        state("main") {
            activators {
                event(AlexaEvent.LAUNCH)
                intent(DialogflowIntent.WELCOME)
                regex("/start")
            }
            
            action {
                reactions.run {
                    sayRandom("Hi!", "Hello there!")
                    say("How are you?")
                    telegram?.image("https://somecutecats.com/cat.jpg")
                }
            }
        }
    }
}
```

## Key features

* Provides [Kotlin-based DSL](https://github.com/just-ai/jaicf-kotlin/wiki/Scenario-DSL) for writing context-aware dialogue scenarios in declarative style.
* Connects to any [voice and text channels](https://github.com/just-ai/jaicf-kotlin/wiki/Channels) like Amazon Alexa, Google Actions, Facebook, Telegram and others.
* Works with any [NLU engines](https://github.com/just-ai/jaicf-kotlin/wiki/Natural-Language-Understanding) like Dialogflow, Rasa and Caila.
* Enables developer to create dialogue scenarios that work [simultaneously in multiple platforms](https://github.com/just-ai/jaicf-kotlin/wiki/Channels#multi-channel-support) without any restrictions of platform-related features.
* Contains JUnit layer to automate dialogue scenarios testing.
* Being a Kotlin app, JAICF driven bot can use any Kotlin or Java features and third-party libraries.
* Can be ran in any servlet container, as [Ktor](https://ktor.io) or [Spring Boot](https://spring.io/projects/spring-boot) application.

## How to start using

Please visit [JAICF documentation](https://github.com/just-ai/jaicf-kotlin/wiki) for [Quick Start](https://github.com/just-ai/jaicf-kotlin/wiki/Quick-Start) and detailed explanations of how to start using this framework in your projects.

## Examples

Here are some [examples](examples) you can find helpful to dive into the framework.

## Contributing

Please see [the contribution guide](CONTRIBUTING.md) to learn how you can be involved in JAICF development.

## Community

You're welcome to [join a Slack community](https://join.slack.com/t/jaicf/shared_invite/zt-clzasfyq-f4gv8hf3JHD4RmpMtrt0Aw) to share your ideas or ask questions regarding the JAICF usage.

## Licensing

JAICF is under [Apache 2.0](LICENSE) license meaning you are free to use and modify it without the need to open your project source code.
