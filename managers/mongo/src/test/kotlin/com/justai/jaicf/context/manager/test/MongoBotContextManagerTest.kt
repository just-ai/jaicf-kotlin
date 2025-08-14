package com.justai.jaicf.context.manager.test

import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.context.manager.mongo.MongoBotContextManager
import com.justai.jaicf.core.test.managers.BotContextManagerBaseTest
import com.mongodb.client.MongoClients
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
class MongoBotContextManagerTestWithExternalDatabase : BotContextManagerBaseTest() {

    override lateinit var manager: BotContextManager

    @BeforeAll
    fun setup() {
        val url = System.getenv("MONGO_URL")
        val client = MongoClients.create(url)
        manager = MongoBotContextManager(client.getDatabase("jaicf").getCollection("contexts"))
    }
}

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class MongoBotContextManagerTestWithEmbeddedMongo : BotContextManagerBaseTest() {

    override lateinit var manager: BotContextManager

    private lateinit var mongo: MongodExecutable

    @BeforeAll
    fun setup() {
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
    fun shutdown() {
        mongo.stop()
    }
}
