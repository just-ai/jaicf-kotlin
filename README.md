[![JAICF](https://github.com/just-ai/jaicf-kotlin/raw/master/docs/images/jaicf_git-07.png)](https://just-ai.com/en/jaicf)

JAICF is a comprehensive enterprise-level framework from [Just AI](https://just-ai.com) for conversational voice assistants and chatbots development using [Kotlin-based DSL](https://github.com/just-ai/jaicf-kotlin/wiki/Scenario-DSL).

<br/>

[![Twitter URL](https://img.shields.io/twitter/url?style=social&url=https%3A%2F%2Fgithub.com%2Fjust-ai%2Fjaicf-kotlin)](https://twitter.com/intent/tweet?text=Try%20JAICF%20-%20a%20Kotlin%20framework%20for%20conversational%20voice%20assistants%20and%20chatbots%20development&url=https://github.com/just-ai/jaicf-kotlin)
[![Build Status](https://travis-ci.org/just-ai/jaicf-kotlin.svg?branch=master)](https://travis-ci.org/just-ai/jaicf-kotlin)
[![Download](https://api.bintray.com/packages/just-ai/jaicf/core/images/download.svg) ](https://bintray.com/just-ai/jaicf/core/_latestVersion)
[![GitHub License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://github.com/just-ai/jaicf-kotlin/blob/master/LICENSE)
[![](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)

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
[Or using Maven configuration](https://github.com/just-ai/jaicf-kotlin/wiki/Installing#maven)

**The latest version is** ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)

## Key features

* Provides [Kotlin-based DSL](https://github.com/just-ai/jaicf-kotlin/wiki/Scenario-DSL) for writing context-aware dialogue scenarios in declarative style.
* Connects to any [voice and text channels](https://github.com/just-ai/jaicf-kotlin/wiki/Channels) like [Amazon Alexa](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/alexa), [Google Actions](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/google-actions), [Yandex Alice](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/yandex-alice), [Slack](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/slack), [Facebook Messenger](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/facebook) and [others](https://github.com/just-ai/jaicf-kotlin/wiki/Channels).
* Works with any [NLU engines](https://github.com/just-ai/jaicf-kotlin/wiki/Natural-Language-Understanding) like [Dialogflow](https://github.com/just-ai/jaicf-kotlin/tree/master/activators/dialogflow), [Rasa](https://github.com/just-ai/jaicf-kotlin/tree/master/activators/rasa) or [CAILA](https://github.com/just-ai/jaicf-kotlin/tree/master/activators/caila).
* Enables developer to create dialogue scenarios that work [simultaneously in multiple platforms](https://github.com/just-ai/jaicf-kotlin/wiki/Channels#multi-channel-support) without any restrictions of platform-related features.
* Contains a [JUnit-based test suite](https://github.com/just-ai/jaicf-kotlin/wiki/Testing) to automate dialogue scenarios testing.
* Being a Kotlin app, JAICF driven bot can use any [Kotlin](https://kotlinlang.org/docs/reference/) or Java features and third-party libraries.
* Can be ran and deployed to [any environment](https://github.com/just-ai/jaicf-kotlin/wiki/Environments) using [Ktor](https://github.com/just-ai/jaicf-kotlin/wiki/Ktor) or [Spring Boot](https://github.com/just-ai/jaicf-kotlin/wiki/Spring-Boot).
* [Embeddable](https://github.com/just-ai/jaicf-kotlin/wiki/Android) to the Android-powered devices.

## How to start using

Please visit [JAICF documentation](https://github.com/just-ai/jaicf-kotlin/wiki) for [Quick Start](https://github.com/just-ai/jaicf-kotlin/wiki/Quick-Start) and detailed explanations of how to start using this framework in your projects.

## Examples

Here are some [examples](examples) you can find helpful to dive into the framework.

## Contributing

Please see [the contribution guide](CONTRIBUTING.md) to learn how you can be involved in JAICF development.

## Community

You're welcome to [join a Slack community](https://join.slack.com/t/jaicf/shared_invite/zt-duq07hx2-w_c71O8BnKDCIqhSXflRjA) to share your ideas or ask questions regarding the JAICF usage.

## Licensing

JAICF is under [Apache 2.0](LICENSE) license meaning you are free to use and modify it without the need to open your project source code.
