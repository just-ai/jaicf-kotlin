package com.justai.jaicf.context.manager.test

import com.justai.jaicf.context.manager.mapdb.JacksonMapDbBotContextManager
import com.justai.jaicf.core.test.managers.BotContextManagerBaseTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.nio.file.Files
import java.nio.file.Paths

class MapDBBotContextManagerTest : BotContextManagerBaseTest() {
    override lateinit var manager: JacksonMapDbBotContextManager

    @BeforeAll
    fun setup() {
        manager = JacksonMapDbBotContextManager(".mapdb")
    }

    @AfterAll
    fun shutdown() {
        manager.close()
        Files.deleteIfExists(Paths.get(".mapdb"))
    }
}

class MapDBBotContextManagerTestWithTempFile : BotContextManagerBaseTest() {

    override lateinit var manager: JacksonMapDbBotContextManager

    @BeforeAll
    fun setup() {
        manager = JacksonMapDbBotContextManager()
    }

    @AfterAll
    fun shutdown() {
        manager.close()
        Files.deleteIfExists(Paths.get(".mapdb"))
    }
}
