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

## Automatic tests

You can also learn how automatic tests are used in this project.
Just try to run `HelloWorldScenarioTest` to see how it works.