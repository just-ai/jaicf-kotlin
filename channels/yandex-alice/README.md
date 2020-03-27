<p align="center">
    <img src="https://i.imgur.com/yJvASKG.png" width="128" height="128"/>
</p>

<h1 align="center">Яндекс Алиса</h1>

Библиотека для создания голосовых навыков для ассистента [Яндекс Алиса](https://yandex.ru/alice) на Kotlin.

## Как использовать

> Это библиотека мультиплатформенного [фреймворка JAICF](https://framework.just-ai.com), исходный код которого доступен [здесь](https://github.com/just-ai/jaicf-kotlin).
> Полная [документация](https://github.com/just-ai/jaicf-kotlin/wiki) фреймворка JAICF описывает, как его использовать для создания голосовых приложений для разных платформ.

### Шаблон проекта

[Здесь](https://github.com/just-ai/alice-jaicf-template) доступен готовый шаблон голосового навыка на JAICF, который можно запустить на сервере Heroku в один клик, а затем изменять код навыка как вам нужно, чтобы он выполнял нужные вам функции.

_Рекомендуем использовать этот шаблон, чтобы сразу получить рабочий сервер для вебхука, а затем продолжать разработку навыка локально._

### Подключение библиотеки

Чтобы создать проект с нуля, вам нужно создать Kotlin проект и добавить в ваш файл _build.gradle.kts_ следующие зависимости:

```kotlin
repositories {
    mavenCentral()
    jcenter()
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-server-netty:1.3.1")

    implementation("com.justai.jaicf:core:$jaicfVersion")
    implementation("com.justai.jaicf:yandex-alice:$jaicfVersion")
}
```

**Замените `$jaicfVersion` последней версией ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

### Создание логики навыка

JAICF дает возможность описывать логику чатбота или голосового приложения в виде сценариев на Kotlin.
В каждом сценарии вы можете оперировать [состояниями и контекстами диалога](https://github.com/just-ai/jaicf-kotlin/wiki/Scenario-DSL), а также [способами процессинга языка](https://github.com/just-ai/jaicf-kotlin/wiki/Natural-Language-Understanding) (интенты, события и регулярные выражения).
Простой пример:

```kotlin
object MainScenario: Scenario() {
    init {
        state("main") {
            activators {
                event(AliceEvent.START)
            }

            action {
                reactions.say("Майор на связи. Докладывайте.")
                reactions.alice?.image(
                    url = "https://i.imgur.com/YOnWzLM.jpg",
                    title = "Майор на связи",
                    description = "Начните сообщение со слова \"Докладываю\"")
            }
        }

        state("report") {
            activators {
                regex("докладываю .+")
            }

            action {
                reactions.run {
                    say("Спасибо.")
                    sayRandom(
                        "Ваш донос зарегистрирован под номером ${random(1000, 9000)}.",
                        "Оставайтесь на месте. Не трогайте вещественные доказательства."
                    )
                    say("У вас есть еще какая-нибудь информация?")
                    buttons("Да", "Нет")
                }
            }

            state("yes") {
                activators {
                    regex("да")
                }

                action {
                    reactions.say("Докладывайте.")
                }
            }

            state("no") {
                activators {
                    regex("нет")
                    regex("отбой")
                }

                action {
                    reactions.sayRandom("Отбой.", "До связи.")
                    reactions.alice?.endSession()
                }
            }

        }
    }
}
```

_Здесь показан простой пример без использования NLU. Можно использовать NLU движки ([Dialogflow](https://github.com/just-ai/jaicf-kotlin/tree/master/activators/dialogflow), [Rasa](https://github.com/just-ai/jaicf-kotlin/tree/master/activators/rasa) и другие)._

> Подробнее о том, какие есть возможности по созданию диалоговых сценариев читайте в [документации](https://github.com/just-ai/jaicf-kotlin/wiki).

### Создание вебхука

Создать вебхук для навыка можно, например, с помощью [Ktor](https://github.com/just-ai/jaicf-kotlin/wiki/Ktor)

```kotlin
val skill = BotEngine(
    model = MainScenario.model,
    activators = arrayOf(
        RegexActivator,
        BaseEventActivator,
        CatchAllActivator
    )
)

fun main() {
    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting("/" to AliceChannel(skill, "ваш OAuth token здесь"))
        }
    }.start(wait = true)
}
```

> Если вы используете картики в вашем навыке, укажите ваш OAuth token, который можно получить [здесь](https://oauth.yandex.ru/authorize?response_type=token&client_id=c473ca268cd749d3a8371351a8f2bcbd).

Чтобы получить URL, который доступен извне, можно использовать [ngrok](https://ngrok.com) - `ngrok http 8000`.
А затем использовать этот вебхук для создания навыка в консоли Яндекс Диалогов.

### API Алисы

JAICF позволяет использовать API любой платформы напрямую, не ограничивая разработчика только функционалом самого фреймворка.
Далее описаны методы API Алисы, которые можно использовать для использования спцифичных для этой платформы функций.

> Специфичные для Алисы реакции доступны через `reactions.alice?`

#### Простые ответы

Простые текстовые и голосовые ответы можно формировать функциями `reactions.say(...)`.

```kotlin
action {
    // Единый текст для отображения и синтеза
    reactions.say("Майор на связи. Докладывайте.")

    // Текст для отображения и для синтеза могут быть разными
    reactions.say(
        text = "Майор на связи. Докладывайте.",
        tts = "докл+адывайте")
}
```

> Подробнее про текстовые ответы читайте в [документации Яндекс Диалогов](https://yandex.ru/dev/dialogs/alice/doc/protocol-docpage/#response)

#### Случайные ответы

Чтобы разнообразить ответы, можно возвращать случайные ответы:

```kotlin
action {
    reactions.sayRandom(
        "Ваш донос зарегистрирован под номером ${random(1000, 9000)}.",
        "Оставайтесь на месте. Не трогайте вещественные доказательства."
    )
}
```

#### Кнопки и ссылки

Для добавления кнопок используйте методы `reactions.buttons(...)`

```kotlin
action {
    // Добавит простые кнопки, которые пропадут после нажатия
    reactions.buttons("Да", "Нет")

    // Добавит кнопки-сслыки, которые не пропадают после нажатия
    reactions.alice?.links(
        "Сайт" to "https://framework.just-ai.com",
        "Документация" to "https://github.com/just-ai/jaicf-kotlin/wiki"
    )

    // Добавит кнопку с произвольной конфигурацией
    reactions.alice?.buttons(
        Button(
            title = "...",
            payload = JsonObject(),
            url = "...",
            hide = false
        )
    )
}
```

> Подробнее про кнопки читайте в [документации Яндекс Диалогов](https://yandex.ru/dev/dialogs/alice/doc/protocol-docpage/#response)

#### Звуки

Чтобы Алиса проиграла нужные вам звуки, сперва их нужно загрузить в консоль Яндекс Дислогов.
Прдробнее об этом в [документации Яндекс Диалогов](https://yandex.ru/dev/dialogs/alice/doc/resource-sounds-upload-docpage/).

Затем можно использовать идентификаторы звуков при создании ответа навыка:

```kotlin
action {
    reactions.alice?.audio("идентификатор звука")
}
```

#### Картинки

Алиса позволяет показывать картинки в ответе от вашего навыка.
Но перед этим требуется загрузить картинки в консоли разработчика и затем использовать полученные индентификаторы.

_Эта библиотека загружает картинки автоматически, чтобы вам не приходилось делать это каждый раз._

```kotlin
action {
    // Картинка без подписи
    reactions.image("https://i.imgur.com/YOnWzLM.jpg")

    // Картинка с заголовком, описанием и кнопкой
    reactions.alice?.image(
        url = "https://i.imgur.com/YOnWzLM.jpg",
        title = "Майор на связи",
        description = "Начните сообщение со слова \"Докладываю\"",
        button = Button(...)
    )
}
```

Как видите, вы можете использовать URL картинки, не загружая ее заранее в консоли Яндекс Диалогов.
Это позволяет создавать навыки, которые отображают дианмические картинки.

Если же вы хотите использовать идентификаторы картинок вместо URL, то можете делать это так:

```kotlin
action {
    reactions.alice?.image(
        Image(
            imageId = "идентификатор картинки",
            title = "Майор на связи",
            description = "Начните сообщение со слова \"Докладываю\"",
            button = Button(...)
        )
    )
}
```

Если есть небходимость получить идентификатор картинки, предварительно загрузив ее при необходимости, можно использовать API:

```kotlin
reactions.alice?.api?.getImageId("https://i.imgur.com/YOnWzLM.jpg")
```

> Подробнее про картинки читайте в [документации Яндекс Диалогов](https://yandex.ru/dev/dialogs/alice/doc/resource-upload-docpage/).

#### Списки

Списки с картинками можно добавить так

```kotlin
action {
    // Создание списка
    reactions.alice?.itemsList(
        header = "Заголовок",
        footer = ItemsList.Footer(text = "...", button = Button(...))
    )
    .addImage(Image(...))  // Добавление элемента списка
    .addImage(Image(...))  // Добавление элемента списка
}
```

#### Завершение сессии

Чтобы Алиса завершила диалог с пользователем

```kotlin
action {
    reactions.sayRandom("Отбой.", "До связи.")
    reactions.alice?.endSession()
}
```

### Детали запроса

Алиса с каждым запросом передает дополнительные данные о пользователе и о самом запросе.

> Подробно об этом написано в [документации Яндекс Диалогов](https://yandex.ru/dev/dialogs/alice/doc/protocol-docpage/#request).

Чтобы получить эти данные, вы можете использовать `request.alice?` в вашем сценарии:

```kotlin
action {
    request.alice?.meta // Метаинформация (локаль, таймзона и тд)
    request.alice?.session // Данные сессии
    request.alice?.request // Данные запроса (NLU, payload от кнопки и тд)
}
```

### Авторизация пользователя

API Алисы поддерживает [OAuth авторизацию пользователей](https://yandex.ru/dev/dialogs/alice/doc/auth/about-account-linking-docpage/).
Вы можете запустить процесс авторизации так

```kotlin
action {
    reactions.alice?.startAccountLinking()
}
```

После завершения авторизации сценарий получит событие `AliceEvent.ACCOUNT_LINKING_COMPLETE`, на который нужно отреагировать [соответственно](https://yandex.ru/dev/dialogs/alice/doc/auth/account-linking-in-custom-skills-docpage/#authorization-complete)

```kotlin
state("auth") {
    activators {
        event(AliceEvent.ACCOUNT_LINKING_COMPLETE)
    }
    action {
        ...
    }
}
```

Бибилиотека автоматически записывает заголовок с токеном авторизации в поле `request.alice?.accessToken`.

# Вопросы и предложения

Если вы нашли баг, то можете написать о нем в [issues](https://github.com/just-ai/jaicf-kotlin/issues) или предложить вашу реализацию и отправить pull request.

Также вы можете присоединиться к [сообществу разработчиков в Slack](https://join.slack.com/t/jaicf/shared_invite/zt-clzasfyq-f4gv8hf3JHD4RmpMtrt0Aw), чтобы задавать вопросы по данной библиотеке.