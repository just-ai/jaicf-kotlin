package com.justai.jaicf.examples.gameclock.test

import com.justai.jaicf.channel.alexa.model.AlexaEvent
import com.justai.jaicf.channel.alexa.model.AlexaIntent
import com.justai.jaicf.channel.googleactions.dialogflow.DialogflowIntent
import com.justai.jaicf.examples.gameclock.scenario.MainScenario
import com.justai.jaicf.test.ScenarioTest
import org.junit.jupiter.api.Test

class MainScenarioTest: ScenarioTest(MainScenario) {

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
    fun `Single player is prohibited`() {
        withCurrentState("/setup/gamers")
        withVariables("gamers" to 1)
        intent("GamersIntent") endsWithState "/setup/gamers/count"
    }

    @Test
    fun `Colors picker goes after players count`() {
        withCurrentState("/setup/gamers")
        withBackState("/setup/colors")
        withVariables("gamers" to 2)

        intent("GamersIntent") endsWithState "/setup/colors"
    }

    @Test
    fun `Only valid colors are allowed`() {
        withCurrentState("/setup/colors")
        withVariables("color" to "pink")
        intent("GamerColorIntent") endsWithState "/setup/colors/color"
    }

    @Test
    fun `Duplicated colors are prohibited`() {
        withCurrentState("/setup/colors")
        withBotContext {
            client["gamers"] = 2
            client["currentGamer"] = 1
        }

        withVariables("color" to "blue")
        intent("GamerColorIntent") endsWithState "/setup/colors"
        intent("GamerColorIntent") endsWithState "/setup/colors/color"
    }

    @Test
    fun `Game starts after color picker`() {
        withBackState("/play")
        withBackState("/setup/complete")
        withCurrentState("/setup/colors")

        withBotContext {
            client["gamers"] = 2
            client["currentGamer"] = 1
        }

        withVariables("color" to "blue")
        intent("GamerColorIntent") endsWithState "/setup/colors"

        withVariables("color" to "green")
        intent("GamerColorIntent") endsWithState "/play"
    }
}