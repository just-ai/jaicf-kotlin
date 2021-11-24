package com.justai.jaicf.context.manager.test

import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.context.manager.mapdb.JacksonMapDbBotContextManager
import com.justai.jaicf.core.test.managers.BotContextManagerBaseTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.nio.file.Files
import java.nio.file.Paths

class JaksonMapDBBotContextManagerTest : BotContextManagerBaseTest() {
    override lateinit var manager: BotContextManager

    @BeforeAll
    fun setup() {
        manager = JacksonMapDbBotContextManager(".mapdb")
    }

    @AfterAll
    fun shutdown() {
        Files.deleteIfExists(Paths.get(".mapdb"))
    }
}

class JaksonMapDBBotContextManagerTestWithTempFile : BotContextManagerBaseTest() {

    override lateinit var manager: BotContextManager

    @BeforeAll
    fun setup() {
        manager = JacksonMapDbBotContextManager()
    }

    @AfterAll
    fun shutdown() {
        Files.deleteIfExists(Paths.get(".mapdb"))
    }
}
