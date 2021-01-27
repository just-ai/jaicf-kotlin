package com.justai.jaicf.examples.gameclock.test

import com.amazon.ask.model.Intent
import com.amazon.ask.model.IntentRequest
import com.amazon.ask.model.Slot
import com.justai.jaicf.channel.alexa.activator.AlexaIntentActivatorContext
import com.justai.jaicf.channel.alexa.alexa
import com.justai.jaicf.channel.alexa.intent
import com.justai.jaicf.channel.alexa.model.AlexaEvent
import com.justai.jaicf.channel.alexa.model.AlexaIntent
import com.justai.jaicf.channel.googleactions.dialogflow.DialogflowIntent
import com.justai.jaicf.examples.gameclock.scenario.MainScenario
import com.justai.jaicf.test.ScenarioTest
import com.justai.jaicf.test.mockjvm.intent
import com.justai.jaicf.test.mockjvm.withContext
import org.junit.jupiter.api.Test

class MainScenarioAlexaTest : ScenarioTest(MainScenario) {

    @Test
    fun `Handles both Alexa and Dialogflow start intents`() {
        event(AlexaEvent.LAUNCH) endsWithState "/launch"
        intent(DialogflowIntent.WELCOME) endsWithState "/launch"
    }

    @Test
    fun `Game setup starts from gamers count`() {
        withCurrentState("/launch")
        intent(AlexaIntent.YES) endsWithState "/setup/gamers"
    }

    @Test
    fun `Wrong gamers count intent fallbacks to the same state`() {
        withCurrentState("/setup/gamers")
        intent(AlexaIntent.FALLBACK) goesToState "/setup/gamers/count" endsWithState "/setup/gamers/count"
    }

    @Test
    fun `Single player is prohibited`() = withContext(alexa.intent) {
        withCurrentState("/setup/gamers")
        intent(alexaIntent("GamersIntent", mapOf("gamers" to "1"))) endsWithState "/setup/gamers/count"
    }

    @Test
    fun `Colors picker goes after players count`() = withContext(alexa.intent) {
        withCurrentState("/setup/gamers")
        withBackState("/setup/colors")

        intent(alexaIntent("GamersIntent", mapOf("gamers" to "2"))) endsWithState "/setup/colors"
    }

    @Test
    fun `Only valid colors are allowed`() = withContext(alexa.intent) {
        withCurrentState("/setup/colors")

        intent(alexaIntent("GamerColorIntent", mapOf("color" to "pink"))) endsWithState "/setup/colors/color"
    }

    @Test
    fun `Duplicated colors are prohibited`() = withContext(alexa.intent) {
        withCurrentState("/setup/colors")
        withBotContext {
            client["gamers"] = 2
            client["currentGamer"] = 1
        }

        withVariables("color" to "blue")
        val alexaIntent = alexaIntent("GamerColorIntent", mapOf("color" to "blue"))
        intent(alexaIntent) endsWithState "/setup/colors"
        intent(alexaIntent) endsWithState "/setup/colors/color"
    }

    @Test
    fun `Game starts after color picker`() = withContext(alexa.intent) {
        withBackState("/play")
        withBackState("/setup/complete")
        withCurrentState("/setup/colors")

        withBotContext {
            client["gamers"] = 2
            client["currentGamer"] = 1
        }

        intent(alexaIntent("GamerColorIntent", mapOf("color" to "blue"))) endsWithState "/setup/colors"
        intent(alexaIntent("GamerColorIntent", mapOf("color" to "green"))) {
            callsDynamic { reactions.playAudio(allAny()) }
            go("/play")
        }
    }
}

fun alexaIntent(intent: String, slots: Map<String, String>) = AlexaIntentActivatorContext(
    IntentRequest.builder().withIntent(
        Intent
            .builder().withName(intent)
            .withSlots(slots.map { (k, v) -> k to Slot.builder().withValue(v).build() }.toMap())
            .build()
    ).build()
)