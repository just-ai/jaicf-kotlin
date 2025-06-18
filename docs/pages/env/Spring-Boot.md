---
layout: default
title: Spring Boot
permalink: Spring-Boot
parent: Environments
---

![](/assets/images/env/spring-boot.png)

[Spring Boot](https://spring.io/projects/spring-boot) makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run".

# Example

[Here is an example project](https://github.com/just-ai/jaicf-jaicp-spring-template) that shows how to use Spring with JAICF and MongoDB.
Please investigate this example to learn how to develop, configure, build and run JAICF projects in production using Spring and Docker.

# How to use

#### 1. Append Spring Boot dependencies to build.gradle

```kotlin
plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.spring") version "1.4.21"

    id("org.springframework.boot") version "2.4.4"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    ...
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    bootJar {
        archiveFileName.set("app.jar")
        mainClass.set("com.justai.jaicf.spring.ApplicationKt")
    }
}

tasks.create("stage") {
    dependsOn("bootJar")
}
```

#### 2. Configure BotEngine

With Spring you can write your scenarios as _components_ to achieve all features of Spring like dependency injection and others.

```kotlin
@Component
class MainScenario(
    private val botConfiguration: BotConfiguration
): Scenario {

    override val model = createModel {
        ...
    }
}
```

And then configure `BotEngine` via Spring configuration

```kotlin
@Configuration
class ApplicationConfiguration(
    private val botConfiguration: BotConfiguration
) {

    @Bean
    fun botApi(mainScenario: MainScenario) = BotEngine(
        scenario = mainScenario,
        activators = arrayOf(RegexActivator),
        conversationLoggers = arrayOf(
            JaicpConversationLogger(botConfiguration.accessToken),
            Slf4jConversationLogger()
        )
    )

    @Bean
    fun jaicpWebhookConnector(botApi: BotApi) = JaicpWebhookConnector(
        botApi = botApi,
        accessToken = botConfiguration.accessToken,
        channels = listOf(ChatWidgetChannel)
    )
}
```

#### 3. Run BotEngine

Create poller for development:

```kotlin
@Component
@Profile("dev")
class Poller(
    private val botApi: BotApi,
    private val botConfiguration: BotConfiguration
): ApplicationRunner, CoroutineScope {
    override val coroutineContext = Dispatchers.Default

    override fun run(args: ApplicationArguments?) {
        launch {
            JaicpPollingConnector(
                botApi = botApi,
                accessToken = botConfiguration.accessToken,
                channels = listOf(ChatWidgetChannel)
            ).runBlocking()
        }
    }
}
```

Or webhook endpoint for production:

```kotlin
@RestController
class WebhookEndpoint(private val jaicpWebhookConnector: JaicpWebhookConnector) {

    @PostMapping("/webhook")
    fun processRequest(@RequestBody request: String) =
        jaicpWebhookConnector.process(request.asHttpBotRequest()).let { response ->
            ResponseEntity
                .status(response.statusCode)
                .headers { it.setAll(response.headers) }
                .body(response.output.toString())
        }
}
```

You may also use [HttpBotChannelServlet](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/channel/http/HttpBotChannelServlet.kt) instead:

```kotlin
@Configuration
@ServletComponentScan
class Context {

    @WebServlet("/")
    class AlexaController: HttpBotChannelServlet(
        AlexaChannel(helloWorldBot)
    )
}
```
