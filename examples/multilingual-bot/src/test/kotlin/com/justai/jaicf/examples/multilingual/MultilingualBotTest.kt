package com.justai.jaicf.examples.multilingual

import com.justai.jaicf.test.BotTest
import org.junit.jupiter.api.Test

class MultilingualBotTest : BotTest(MultilingualBotEngine) {

    @Test
    fun `english scenario integrational test`() {
        query("/start") responds "Hello! Please, select your language"
        query("English") responds "Hello there! I can help get current Bitcoin to USD exchange rate."
        query("Bitcoin to USD") responds "You can get 20000 USD for 1 Bitcoin."
    }

    @Test
    fun `russian scenario integrational test`() {
        query("/start") responds "Hello! Please, select your language"
        query("Русский") responds "Приветствую! Я могу помочь получить курс Биткоина к Доллару США."
        query("Курс биткоина") responds "Сейчас за 1 Bitcoin можно получить 20000 USD"

        query("Я бы хотел узнать, какая завтра погода")
    }

    @Test
    fun `routing functional test`() {
        query("/start") responds "Hello! Please, select your language"
        query("English") responds "Hello there! I can help get current Bitcoin to USD exchange rate."

        query("Select language") responds "Hello! Please, select your language"
        query("Русский") responds "Приветствую! Я могу помочь получить курс Биткоина к Доллару США."

        query("Выбор языка") responds "Hello! Please, select your language"
        query("what's the current bitcoin price?") responds "Hello there! I can help get current Bitcoin to USD exchange rate."

        query("Select language") responds "Hello! Please, select your language"
        query("Русский") responds "Приветствую! Я могу помочь получить курс Биткоина к Доллару США."
    }

    @Test
    fun `routing nlp test`() {
        query("/start") responds "Hello! Please, select your language"
        query("English") responds "Hello there! I can help get current Bitcoin to USD exchange rate."
        query("so how much one bitcoin cost?") responds "You can get 20000 USD for 1 Bitcoin."


        query("Select language") responds "Hello! Please, select your language"
        query("Русский") responds "Приветствую! Я могу помочь получить курс Биткоина к Доллару США."
        query("Так сколько сейчас стоит один биток?") responds "Сейчас за 1 Bitcoin можно получить 20000 USD"
    }
}