package com.justai.jaicf.activator.rasa.test

import com.justai.jaicf.activator.rasa.api.RasaApi
import com.justai.jaicf.activator.rasa.api.RasaParseMessageRequest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class RasaParseMessageIT {

    private val api = RasaApi("https://polar-sierra-70489.herokuapp.com/")

    @Test
    fun testParseMessage() {
        val response = api.parseMessage(RasaParseMessageRequest("hi"))
        assertNotNull(response)
        assertNotNull(response?.intent)
    }
}