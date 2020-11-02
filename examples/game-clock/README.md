# Game clock example

This example shows how to create a complex voice-first game for both Alexa and Google Assistant with images, buttons and audio player support.

## Description

This voice skill implements a board game voice tool that allows to track each gamer's turn timing.

Once the gamer finishes their turn, they speak _"Alexa, next"_ or _"Okay Google, pass the turn"_.
Voice skill responds with summary of the turn and starts timer for the next gamer.

At the end of the game, one of the gamers commands with _"Alexa, stop music"_ or _"Okay Google, finish the game"_.

## How to use

This example uses [Alexa](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/alexa) and [Google Actions](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/google-actions) connectors.
Thus to run this example, you have to create Alexa custom skill and Dialogflow agent.

### 1. Create a JAICP project

The most easiest way is to create a new _JAICF project_ in [JAICP Application Panel](https://app.jaicp.com).
Then add two channels on the _Channels_ menu:

- Amazon Alexa
- Dialogflow

_This generates webhook URLs that should be used in Alexa Console and Dialogflow on the next steps._

Obtain your JAICP project's API key from _Project Properties_ -> _Location_.

### 2. Run JaicpPoller locally

Run `JaicpPoller.kt` file and paste your JAICP API key.

### 3. Create Alexa custom skill

1. Create a new Alexa custom skill in the Amazon Developer Console as described [here](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/alexa#5-configure-alexa-custom-skill).
Provide a webhook URL of the skill obtained on the step 1.
2. Then go to JSON Editor on the left side bar and paste here the content of `alexa.json` file from this project's `model` folder.
3. Enable Audio Player feature in the Interfaces.
4. Click on Build Model and try to test the skill on the Test tab using "Start [your skill name]" command.

You can also use a physical Amazon Echo device or Echo application on your smartphone.

### 4. Create Google Action

1. Create a new [Dialogflow agent](https://dialogflow.com) and import a `dialogflow.zip` in its settings.
2. Go to Fulfillment in the left side bar and paste your Webhook URL obtained on step 1.
3. Go through an each intent and enable Fulfillment option.
4. Click the _Integrations_ -> _Google Assistant_ -> _Test_ to test your action.

## Automatic tests

You can also learn how automatic tests are used in this project.
Just try to run `MainScenarioTest` to see how it works.