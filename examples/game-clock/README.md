# Game clock example

This example shows how to create a complex voice-first game for both Alexa and Google Assistant with audio player support.

## Description

This voice skill implements a board game voice tool that allows to track each gamer's turn timing.
Once the gamer finishes their turn, they speak "Alexa, next" or "Okay Google, pass the turn".
Voice skill responds with summary of the turn and starts timer for the next gamer.

At the end of the game, one of the gamers commands with "Alexa, stop music" or "Okay Google, finish the game".

## How to use

This example uses [Alexa](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/alexa) and [Google Actions](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/google-actions) connectors.
Thus to run this example, you have to create Alexa custom skill and Google Action.

Before you begin, just start `Webhook.kt` and obtain a public URL using [ngrok](https://ngrok.com) this way:

`ngrok http 8000`

### Alexa custom skill

1. Create a new Alexa custom skill in the Amazon Developer Console as described [here](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/alexa#5-configure-alexa-custom-skill).
Provide a URL endpoint of the skill obtained on the step below.
Append `/alexa` at the end of the URL.
2. Then go to JSON Editor on the left side bar and paste here the content of `alexa.json` file from this project.
3. Enable Audio Player feature in the Interfaces
4. Click on Build Model and try to test the skill on the Test tab using "Start [your skill name]" command

You can also use a physical Amazon Echo device or Echo application on your smartphone.

### Google Action

1. Create a new [Dialogflow agent](https://dialogflow.com) and import a `dialogflow.zip` in its settings.
2. Go to Fulfillment in the left side bar and paste your Webhook URL with appended `/actions` path to the end of URL. Click on Save.
3. Go through an each intent and enable Fulfillment for each.
4. Click the Google Assistant link on the right side bar to test your action.

## Automatic tests

You can also learn how automatic tests are used in this project.
Just try to run `MainScenarioTest` to see how it works.