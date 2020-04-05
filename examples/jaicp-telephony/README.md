# Telephony bot example

This example shows how to create a simple telephony bot and process incoming calls

## Description

This is a simplest bot with examples, how to integrate with JAICP and how to process incoming call requests
with events and reactions. It logs when user called or hung up the phone, 
plays audio if asked and can hang up the phone by itself.

## How to use

Make three simple steps in JAICP Web Interface:
1. Create a project in JAICP Web Interface
2. Add a SIP trunk (a telephone line over IP)
3. Add a telephony channel for created trunk 

Then you can connect to your bot either using webhook, or a long-polling connection. Let's see a polling connection:
```kotlin
JaicpPollingConnector(
    botApi = citiesGameBot,
    accessToken = accessToken,
    channels = listOf(TelephonyChannel)
).runBlocking()
```
Access token can be obtained in Project Settings in JAICP Web Interface.

### Little about SIP

SIP trunk is telephone line over IP, which is controlled by SIP (Session Initiation Protocol).
There are many providers, using which you can create a free SIP trunk and test your bot.

For example, you can use [Zadarma](https://zadarma.com/en/) to get a virtual number.
 
> Next part of guide is covering how to manage your telephony channels in JAICP infrastructure. 
> It will be available soon.   

