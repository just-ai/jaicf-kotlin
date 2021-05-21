<p style="text-align: center">
    <img src="https://www.routeoneconnect.com/wp-content/uploads/2020/03/icon-amazon-lex.png" height="128" width="128" alt="Lex logo"/>
</p>

<h1 style="text-align: center">Lex NLU activator</h1>

Allows using [Amazon Lex](https://aws.amazon.com/lex/) NLU engine as a states activator in JAICF.

_Built on top of [Amazon Lex API Client for Java](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/)._

## How Lex works

You can read how Lex works [here](https://docs.aws.amazon.com/lex/latest/dg/how-it-works.html)

## How to use

#### 1. Include Lex dependency to your _build.gradle_

```kotlin
implementation("com.just-ai.jaicf:lex:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest
version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

#### 2. Use Lex `activator` in your scenario actions

```kotlin
state("launch") {
    activators {
        intent("BookHotel")
    }

    action(lex) {
        // Recognised named entities
        val slots = activator.slots

        // Data of recognized intent 
        val recognizedIntent = activator.recognizedIntentData
    }
}
```

#### 3. Sign up in the AWS Management Console

To create an AWS account, open [Aws Management sign up page](https://portal.aws.amazon.com/billing/signup) and follow
the online instructions.

Part of the sign-up procedure involves receiving a phone call and entering a verification code on the phone keypad.

#### 4. Sign in Lex console

Open [Lex Console](https://console.aws.amazon.com/lexv2/home) and sign in. Select the second version of the console.

#### 5. Create a bot

You can create a bot using this [blueprint](https://docs.aws.amazon.com/lex/latest/dg/ex-book-trip.html) or create your
bot from empty. For better understanding, you can read
this [topic](https://docs.aws.amazon.com/lex/latest/dg/gs-console.html).

#### 6. Configure Lex activator

A bot ID can be found on the main page of your bot in the "Bot Details" block. Also, to use the bot, you need to create
an alias and associate it with the version of the bot. The region argument must equal the region of your bot. The locale
is the language for which you are creating the bot.

To find your credentials, you need to click on your username in the upper right corner and select "My Security
Credentials."
On the page that opens, select "Access keys" and click "Create new access key."
In the form that opens, select "Show access key," then save the access key ID and secret access key.

```kotlin
val lexActivator = LexIntentActivator.Factory(
    LexConnector(
        LexBotConfig(
            "BOT_ID",
            "BOT_ALIAS_ID",
            Region.EU_WEST_2,
            Locale.US
        ),
        AwsBasicCredentials.create(
            "access_key_id",
            "secret_access_key"
        )
    )
)
```

## Slot filling

Lex provides a slot filling feature that allows your JAICF scenario to resolve the intent after all the necessary intent
parameters (slots) have been received from the user. You don't need to make special changes in your scenarios to use
this feature - JAICF handles all slot filling-related logic for you.

> Please note that Lex may cancel the slot fill process if the confirmation has been canceled.
> In this case, the decline response configured in the Lex Console will be sent and no state will be selected.
