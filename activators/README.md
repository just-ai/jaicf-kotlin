# JAICF activators

Activators in JAICF try to handle user's request and _activate_ a corresponding scenario's state.

Here is a list of ready to use activators that implement natural language processing algorithms to recognise user's intents.

* [Caila](https://github.com/just-ai/jaicf-kotlin/tree/master/activators/caila)
* [Dialogflow](https://github.com/just-ai/jaicf-kotlin/tree/master/activators/dialogflow)
* [Rasa AI](https://github.com/just-ai/jaicf-kotlin/tree/master/activators/rasa)

# How to use

Please refer to the selected activator page to learn how to use it with your JAICF conversational agent.

In general all you have to do - is to append an activator to your agent's configuration:

```kotlin
val dialogflowActivator = DialogflowIntentActivator.Factory(
    DialogflowConnector(DialogflowAgentConfig(
        language = "en",
        credentialsResourcePath = "/dialogflow_account.json"
    ))
)

val cailaActivator = CailaIntentActivator.Factory(
    CailaNLUSettings(
        accessToken = "<your_jaicp_access_token>", 
        confidenceThreshold = 0.2 
))


val helloWorldBot = BotEngine(
    model = HelloWorldScenario.model,
    activators = arrayOf(
        AlexaActivator,
        dialogflowActivator,
        cailaActivator,
        RegexActivator,
        BaseEventActivator,
        CatchAllActivator
    )
)
```

Some activators require an additional configuration parameters to be used (like Dialogflow activator in this example).

# Multiple activators

Also you can note that JAICF allows to use multiple activators.
This means that JAICF will invoke it one by one starting from the top-level activator for each user's request.
Once the activator can handle this request and there is a corresponding state of the dialogue, JAICF stops activators traversing and invokes a state's action.

_That is why the order of activators in activators' array matters._

> Learn more about activators [here](https://github.com/just-ai/jaicf-kotlin/wiki/Natural-Language-Understanding).
