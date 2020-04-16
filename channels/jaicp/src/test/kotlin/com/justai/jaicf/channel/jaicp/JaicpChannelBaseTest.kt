package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.catchall.CatchAllActivator
import com.justai.jaicf.channel.facebook.facebook
import com.justai.jaicf.channel.jaicp.reactions.chatapi
import com.justai.jaicf.channel.jaicp.reactions.chatwidget
import com.justai.jaicf.channel.jaicp.reactions.telephony
import com.justai.jaicf.model.scenario.Scenario
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.TestMethodOrder
import java.io.File
import java.io.FileInputStream
import kotlin.test.fail

@TestMethodOrder(MethodOrderer.Alphanumeric::class)
open class BaseTest {
    private lateinit var testName: String
    private lateinit var testNumber: String
    private lateinit var testPackage: String
    protected val json = Json(JsonConfiguration.Stable)
    protected val echoBot = BotEngine(
        model = EchoScenario.model,
        activators = arrayOf(CatchAllActivator)
    )

    @BeforeEach
    fun setTestName(testInfo: TestInfo) {
        testName = testInfo.displayName
        testNumber = testName.split(" ")[0]
        testPackage = testName.split(" ")[1]
    }

    private fun getResource(name: String): File {
        val path = "$RESOURCES_PATH/$testPackage/$testNumber/$name"
        val file = File(path)
        if (!file.exists()) {
            fail("File for path $path does not exist")
        }
        return file
    }

    protected fun getResourceAsString(name: String): String {
        return getResource(name).readText()
    }

    protected fun getResourceAsInputStream(name: String): FileInputStream {
        return getResource(name).inputStream()
    }

    companion object {
        const val RESOURCES_PATH = "./src/test/resources"
    }
}

private object EchoScenario : Scenario() {
    init {
        state("echo") {
            globalActivators {
                catchAll()
            }
            action {
                reactions.say("You said: ${request.input} from ${reactions::class.simpleName}")
            }
        }
    }
}
