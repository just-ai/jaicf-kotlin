# Aimybox Channel

Allows to create skills for custom voice assistant applications built on top of [Aimybox SDK](https://aimybox.com).

## How to use

#### 1. Include Aimybox dependency to your _build.gradle_

```kotlin
implementation("com.justai.jaicf:aimybox:$jaicfVersion")
```

#### 2. Use Aimybox `request` and `reactions` in your scenarios' actions

```kotlin
action {
    // Arbitrary JSON object passed from the device
    val data = request.aimybox?.data

    // Add custom replies
    reactions.aimybox?.question(true)
    reactions.aimybox?.say(text = "Hello!", tts = "hello, how are you?")
    reactions.aimybox?.buttons(UrlButton("Open websitte", "https://address.com"))
    
    // Or use standard response builders
    reactions.say("How are you?")
    reactions.buttons("Good", "Bad")
}
```

> Please refer to the [Aimybox HTTP API](https://help.aimybox.com/en/category/http-api-1vrvqsw/) to learn more about available reply types.

#### 3. Create and run Aimybox webhook

Using [Ktor](https://ktor.io)

```kotlin
fun main() {
    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting("/" to AimyboxChannel(helloWorldBot))
        }
    }.start(wait = true)
}
```

Using [Spring Boot](https://spring.io/projects/spring-boot)

```kotlin
@Bean
fun aimyboxServlet() {
    return ServletRegistrationBean(
        HttpBotChannelServlet(AimyboxChannel(helloWorldBot)),
        "/"
    ).apply {
        setLoadOnStartup(1)
    }
}
```

#### 4. Configure Aimybox

Then you can use the public webhook URL (using [ngrok](https://ngrok.com) for example) to register a custom voice skill via [Aimybox Console](https://app.aimybox.com) or provide this URL directly to the Aimybox initialisation block of your mobile application.