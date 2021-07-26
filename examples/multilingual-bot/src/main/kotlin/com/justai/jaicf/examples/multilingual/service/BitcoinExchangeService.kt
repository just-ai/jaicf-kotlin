package com.justai.jaicf.examples.multilingual.service

import com.justai.jaicf.examples.multilingual.util.HttpClient
import com.justai.jaicf.examples.multilingual.util.Jackson
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking

object BitcoinExchangeService {
    private const val endpoint = "https://blockchain.info/ticker"

    fun getBitcoinToUSD(testMode: Boolean): String = runBlocking {
        if (testMode) return@runBlocking "20000"
        Jackson.readTree(HttpClient.get<String>(endpoint))["USD"]["last"].toString()
    }

}