package com.justai.jaicf.examples.mrhappy.activator

import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.intent.BaseIntentActivator
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.api.hasIntent
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel

class MrHappyActivator(model: ScenarioModel) : BaseIntentActivator(model) {

    override val name = "mrHappyActivator"

    // Accept both raw text queries (ConsoleChannel) and pre-classified intents
    override fun canHandle(request: BotRequest) = request.hasQuery() || request.hasIntent()

    override fun recogniseIntent(botContext: BotContext, request: BotRequest): List<IntentActivatorContext> {
        val lower = request.input.trim().toLowerCase()
        val intent = when {
            lower.startsWith("!")              -> "shell"
            lower.startsWith("save ")          -> "memory.save"
            MEMORY_RE.containsMatchIn(lower)   -> "memory.search"
            STATUS_RE.containsMatchIn(lower)   -> "status"
            REFLECT_RE.containsMatchIn(lower)  -> "reflect"
            LIFE_RE.containsMatchIn(lower)     -> "life"
            BUSINESS_RE.containsMatchIn(lower) -> "business"
            HOME_RE.containsMatchIn(lower)     -> "home"
            else                               -> "chat"
        }
        return listOf(IntentActivatorContext(1f, intent))
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = MrHappyActivator(model)

        private val MEMORY_RE   = Regex("""memory|don't forget|recall\b""")
        private val STATUS_RE   = Regex("""^!?status\b""")
        private val REFLECT_RE  = Regex("""^reflect${'$'}|morning brief|daily brief|jai axzora""")
        private val LIFE_RE     = Regex("""health|routine|exercise|sleep|energy|fitness""")
        private val BUSINESS_RE = Regex("""business|axzora|revenue|hp token|cricket|ammer|picoclaw""")
        private val HOME_RE     = Regex("""mercedes|home assistant|\beqs\b|lights?|temperature""")
    }
}
