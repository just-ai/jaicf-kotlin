# Hello world example

This simple example shows a basic principles of JAICF.

## How to use

Start `Console.kt` to talk with hello world bot in the terminal.

This example also provides other channels to test like `Telegram.kt`, `Slack.kt` and `Facebook.kt`.
Just replace configuration parameters in these channels configuration with your own.

Refer to the appropriate channel configuration to learn how to create a channel and obtain its parameters.

## Sub-scenarios

You can learn how sub-scenarios are used in this example.
Look into `HelperScenario` to see how `askForName` is used to invoke a helper scenario and return a result back to the `HelloWorldScenario` once the user responds with their name.

## Channels

You can connect this project to any supported channel like Amazon Alexa, Telegram and others.

The most easiest way to do this is to create a new JAICF project in [JAICP Application Panel](https://app.jaicp.com).
Then go to the _Channels menu_ and add one or more channels (Amazon Alexa, Telegram, Chat Widget and etc).

After that you can run `JaicpPoller.kt` locally to test your project via these channels.

> It will ask you for JAICP API key.
You can obtain it through _Project properties_ -> _Location_ menu.

Note that you have to create a corresponding configuration for each channel you've added.
In the `models` folder you can find appropriate settings that should be imported to [Dialogflow](https://dialogflow.com) agent and [Amazon Alexa Console](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/alexa#5-configure-alexa-custom-skill).

## Automatic tests

You can also learn how automatic tests are used in this project.
Just try to run `HelloWorldScenarioTest` to see how it works.
