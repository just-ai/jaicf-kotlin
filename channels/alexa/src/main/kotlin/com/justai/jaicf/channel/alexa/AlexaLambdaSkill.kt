package com.justai.jaicf.channel.alexa

import com.amazon.ask.SkillStreamHandler
import com.justai.jaicf.api.BotApi

abstract class AlexaLambdaSkill(
    botApi: BotApi,
    dynamoDBTableName: String? = null
): SkillStreamHandler(AlexaSkill.create(botApi, dynamoDBTableName))