package com.justai.jaicf.channel.alexa

import com.amazon.ask.Skill
import com.amazon.ask.Skills
import com.justai.jaicf.api.BotApi

object AlexaSkill {

    fun create(
        botApi: BotApi,
        dynamoDBTableName: String? = null
    ): Skill = Skills.standard()
        .addRequestHandler(AlexaRequestHandler(botApi, !dynamoDBTableName.isNullOrEmpty()))
        .withTableName(dynamoDBTableName)
        .build()
}