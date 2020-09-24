package com.justai.jaicf.channel.alexa

import com.amazon.ask.Skill
import com.amazon.ask.Skills
import com.justai.jaicf.api.BotApi

object AlexaSkill {

    fun create(botApi: BotApi): Skill = Skills.standard()
        .addRequestHandler(AlexaRequestHandler(botApi))
        .build()
}