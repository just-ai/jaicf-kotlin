package com.justai.jaicf.test

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.catchall.CatchAllActivator
import com.justai.jaicf.activator.event.BaseEventActivator
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.context.manager.InMemoryBotContextManager
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.model.scenario.ScenarioModel

/**
 * Main abstraction for particular [ScenarioModel] test.
 * Unlike [BotTest] it creates [BotEngine] instance using provided [ScenarioModel] and default activators ([BaseIntentActivator], [RegexActivator]. [BaseEventActivator] and [CatchAllActivator]),
 *
 * @param scenario a [ScenarioModel] to be tested
 * @see BaseIntentActivator
 * @see BaseEventActivator
 * @see RegexActivator
 * @see CatchAllActivator
 */
open class ScenarioTest(
    scenario: Scenario
): BotTest(
    BotEngine(scenario, InMemoryBotContextManager, DEFAULT_ACTIVATORS)
) {
    companion object {
        private val DEFAULT_ACTIVATORS = arrayOf(
            BaseIntentActivator,
            RegexActivator,
            BaseEventActivator,
            CatchAllActivator
        )
    }
}