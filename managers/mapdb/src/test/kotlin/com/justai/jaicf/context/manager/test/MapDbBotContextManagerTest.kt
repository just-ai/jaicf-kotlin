package com.justai.jaicf.context.manager.test

import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.context.manager.mapdb.MapDbBotContextManager
import com.justai.jaicf.core.test.managers.BotContextManagerBaseTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.nio.file.Files
import java.nio.file.Paths

class MapDBBotContextManagerTest : BotContextManagerBaseTest() {
    override lateinit var manager: MapDbBotContextManager

    @BeforeAll
    fun setup() {
        manager = MapDbBotContextManager(".mapdb")
    }

    @AfterAll
    fun shutdown() {
        manager.close()
        Files.deleteIfExists(Paths.get(".mapdb"))
    }
}

class MapDBBotContextManagerTestWithTempFile : BotContextManagerBaseTest() {

    override lateinit var manager: MapDbBotContextManager

    @BeforeAll
    fun setup() {
        manager = MapDbBotContextManager()
    }

    @AfterAll
    fun shutdown() {
        manager.close()
        Files.deleteIfExists(Paths.get(".mapdb"))
    }
}
