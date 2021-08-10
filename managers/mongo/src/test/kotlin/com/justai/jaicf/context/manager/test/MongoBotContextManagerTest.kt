package com.justai.jaicf.context.manager.test

import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.mongo.MongoBotContextManager
import com.mongodb.client.MongoClients
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class MongoBotContextManagerTestBase {

    internal lateinit var manager: MongoBotContextManager

    @BeforeAll
    internal abstract fun setup()

    @AfterAll
    internal open fun shutdown() {}

    @Test
    fun testSave() {
        val context = BotContext("client1").apply {
            result = "some result"
            session["key1"] = "some value"
        }

        manager.saveContext(context, null, null, RequestContext.DEFAULT)

        val result = manager.loadContext(EventBotRequest("client1", "event"), RequestContext.DEFAULT)

        assertNotNull(result)
        assertEquals(context.result, result.result)
        assertEquals(context.session, result.session)
    }

    @Test
    fun testSaveCustomBean() {
        val context = BotContext("client2").apply {
            result = CustomValue(1)
            session["value"] = CustomValue(CustomValue(2))
        }

        manager.saveContext(context, null, null, RequestContext.DEFAULT)

        val result = manager.loadContext(EventBotRequest("client2", "event"), RequestContext.DEFAULT)

        assertNotNull(result)
        assertTrue(result.result is CustomValue)
        assertTrue(result.session["value"] is CustomValue)
    }
}

@EnabledIfEnvironmentVariable(named = "mongo", matches = "external")
class MongoBotContextManagerTestWithExternalDatabase : MongoBotContextManagerTestBase() {

    @BeforeAll
    internal override fun setup() {
        val client = MongoClients.create("mongodb://localhost:27017")
        manager = MongoBotContextManager(client.getDatabase("jaicf").getCollection("contexts"))
    }
}

@DisabledIfEnvironmentVariable(named = "mongo", matches = "external")
class MongoBotContextManagerTestWithEmbeddedMongo : MongoBotContextManagerTestBase() {

    private lateinit var mongo: MongodExecutable

    @BeforeAll
    internal override fun setup() {
        val port = Network.getFreeServerPort()
        MongodConfig.builder()
            .version(Version.Main.PRODUCTION)
            .net(Net(port, false))
            .build().let { config ->
                mongo = MongodStarter.getDefaultInstance().prepare(config)
                mongo.start()
            }

        val client = MongoClients.create("mongodb://localhost:$port")
        manager = MongoBotContextManager(client.getDatabase("jaicf").getCollection("contexts"))
    }

    @AfterAll
    internal override fun shutdown() {
        mongo.stop()
    }
}
