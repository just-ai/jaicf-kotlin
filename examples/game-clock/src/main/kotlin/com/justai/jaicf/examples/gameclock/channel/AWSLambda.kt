package com.justai.jaicf.examples.gameclock.channel

import com.justai.jaicf.channel.alexa.AlexaLambdaSkill
import com.justai.jaicf.examples.gameclock.gameClockBot

class AWSLambda: AlexaLambdaSkill(botApi = gameClockBot, dynamoDBTableName = "GameClock")