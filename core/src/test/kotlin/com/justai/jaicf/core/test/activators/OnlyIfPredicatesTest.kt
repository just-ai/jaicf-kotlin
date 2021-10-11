package com.justai.jaicf.core.test.activators

import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.model.activation.onlyFrom
import com.justai.jaicf.model.activation.onlyIfInClient
import com.justai.jaicf.model.activation.onlyIfInSession
import com.justai.jaicf.model.activation.onlyIfNotInClient
import com.justai.jaicf.model.activation.onlyIfNotInSession
import com.justai.jaicf.test.ScenarioTest
import com.justai.jaicf.test.reactions.TestReactions
import org.junit.jupiter.api.Test

private class BotRequestType1(clientId: String, input: String) : QueryBotRequest(clientId, input)
private class BotRequestType2(clientId: String, input: String) : QueryBotRequest(clientId, input)

private val channelTypeToken1 = ChannelTypeToken<BotRequestType1, TestReactions>()
private val channelTypeToken2 = ChannelTypeToken<BotRequestType2, TestReactions>()

private val contextTestScenario = Scenario {
    state("context test 1") {
        activators {
            regex("context test").onlyIfInClient<Int>("age") { it >= 18 }
        }
    }

    state("context test 2") {
        activators {
            regex("context test").onlyIfInClient<Int>("age")
        }
    }

    state("context test 3") {
        activators {
            regex("context test").onlyIfInClient("age")
        }
    }

    state("context test 4") {
        activators {
            regex("context test").onlyIfNotInClient("age").onlyIfNotInSession("age")
        }
    }

    state("context test 5") {
        activators {
            regex("context test").onlyIfInSession<Int>("age") { it >= 18 }
        }
    }

    state("context test 6") {
        activators {
            regex("context test").onlyIfInSession<Int>("age")
        }
    }

    state("context test 7") {
        activators {
            regex("context test").onlyIfInSession("age")
        }
    }
}

private val typeTestScenario = Scenario {
    state("type test 1") {
        activators {
            regex("type test").onlyFrom(channelTypeToken1)
        }
    }

    state("type test 2") {
        activators {
            regex("type test").onlyFrom(channelTypeToken2)
        }
    }
}

private val onlyIfScenario = Scenario {
    append("context test", contextTestScenario, modal = true)
    append("type test", typeTestScenario, modal = true)
}

class OnlyIfPredicatesTest : ScenarioTest(onlyIfScenario) {
    @Test
    fun `onlyIfInClient should activate by key, type and predicate matches`() {
        withCurrentContext("/context test")
        withBotContext { client["age"] = 21 }
        query("context test") goesToState "/context test/context test 1"
    }

    @Test
    fun `onlyIfInClient should activate by key and type matches`() {
        withCurrentContext("/context test")
        withBotContext { client["age"] = 17 }
        query("context test") goesToState "/context test/context test 2"
    }

    @Test
    fun `onlyIfInClient should activate by key matches`() {
        withCurrentContext("/context test")
        withBotContext { client["age"] = "something" }
        query("context test") goesToState "/context test/context test 3"
    }

    @Test
    fun `onlyIfNotInClient should activate by key doesn't match`() {
        withCurrentContext("/context test")
        query("context test") goesToState "/context test/context test 4"
    }

    @Test
    fun `onlyIfInSession should activate by key, type and predicate matches`() {
        withCurrentContext("/context test")
        withBotContext { session["age"] = 21 }
        query("context test") goesToState "/context test/context test 5"
    }

    @Test
    fun `onlyIfInSession should activate by key and type matches`() {
        withCurrentContext("/context test")
        withBotContext { session["age"] = 17 }
        query("context test") goesToState "/context test/context test 6"
    }

    @Test
    fun `onlyIfInSession should activate by key matches`() {
        withCurrentContext("/context test")
        withBotContext { session["age"] = "something" }
        query("context test") goesToState "/context test/context test 7"
    }

    @Test
    fun `onlyFrom should activate by request type matches`() {
        withCurrentContext("/type test")
        process(BotRequestType1(botContext.clientId, "type test")) goesToState "/type test/type test 1"

        withCurrentContext("/type test")
        process(BotRequestType2(botContext.clientId, "type test")) goesToState "/type test/type test 2"
    }
}