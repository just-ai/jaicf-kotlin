package com.justai.jaicf.channel.jaicp.bargein

import com.justai.jaicf.BotEngine
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.hook.BotRequestHook
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Tests that barge-in hooks are not duplicated when multiple TelephonyChannel instances
 * are created with the same BotEngine (simulating multiple telephony channels in JAICP).
 */
class BargeInHookDuplicationTest {

    private val scenario = Scenario {
        fallback {
            reactions.say("fallback")
        }
    }

    @Test
    @Suppress("UNUSED_VARIABLE")
    fun `hooks should be registered only once when multiple TelephonyChannels are created with same BotEngine`() {
        val botEngine = BotEngine(scenario)

        // Simulate what JaicpConnector does when multiple resterisk channels are configured
        // Variables are intentionally unused - we only care about hook registration side effects
        val channel1 = TelephonyChannel(botEngine)
        val channel2 = TelephonyChannel(botEngine)
        val channel3 = TelephonyChannel(botEngine)

        // Count BotRequestHook listeners - should be exactly 1 (from BargeInProcessor)
        val botRequestHooks = botEngine.hooks.actions[BotRequestHook::class]
        assertEquals(1, botRequestHooks?.size ?: 0,
            "Expected exactly 1 BotRequestHook listener, but found ${botRequestHooks?.size ?: 0}. " +
            "Hooks are being duplicated when creating multiple TelephonyChannel instances.")
    }

    @Test
    fun `hooks should be registered separately for different BotEngines`() {
        val botEngine1 = BotEngine(scenario)
        val botEngine2 = BotEngine(scenario)

        // Create channels for different engines
        TelephonyChannel(botEngine1)
        TelephonyChannel(botEngine2)

        // Each engine should have its own hooks
        val hooks1 = botEngine1.hooks.actions[BotRequestHook::class]
        val hooks2 = botEngine2.hooks.actions[BotRequestHook::class]

        assertEquals(1, hooks1?.size ?: 0, "BotEngine1 should have exactly 1 BotRequestHook listener")
        assertEquals(1, hooks2?.size ?: 0, "BotEngine2 should have exactly 1 BotRequestHook listener")
    }

    @Test
    fun `hooks should be registered only once when using TelephonyChannel Factory`() {
        val botEngine = BotEngine(scenario)
        val factory = TelephonyChannel.Factory()

        // Simulate JaicpConnector creating multiple channels from the same factory
        factory.create(botEngine, mockLiveChatProvider())
        factory.create(botEngine, mockLiveChatProvider())
        factory.create(botEngine, mockLiveChatProvider())

        val botRequestHooks = botEngine.hooks.actions[BotRequestHook::class]
        assertEquals(1, botRequestHooks?.size ?: 0,
            "Expected exactly 1 BotRequestHook listener when using Factory, but found ${botRequestHooks?.size ?: 0}")
    }

    @Test
    fun `hooks should be registered only once with mixed direct and factory creation`() {
        val botEngine = BotEngine(scenario)

        // Mix of direct creation and factory creation
        TelephonyChannel(botEngine)
        TelephonyChannel.Factory().create(botEngine, mockLiveChatProvider())
        TelephonyChannel(botEngine)

        val botRequestHooks = botEngine.hooks.actions[BotRequestHook::class]
        assertEquals(1, botRequestHooks?.size ?: 0,
            "Expected exactly 1 BotRequestHook listener with mixed creation, but found ${botRequestHooks?.size ?: 0}")
    }

    private fun mockLiveChatProvider() = object : com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider {}
}