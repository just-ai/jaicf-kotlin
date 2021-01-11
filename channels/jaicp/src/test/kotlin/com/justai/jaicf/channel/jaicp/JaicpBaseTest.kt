package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import kotlinx.serialization.json.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.TestMethodOrder
import java.io.File
import java.io.FileInputStream
import kotlin.test.fail

@Suppress("MemberVisibilityCanBePrivate")
@TestMethodOrder(MethodOrderer.Alphanumeric::class)
open class JaicpBaseTest {
    protected lateinit var testName: String
    protected lateinit var testNumber: String
    protected lateinit var testPackage: String

    protected val request: HttpBotRequest get() = HttpBotRequest(getResourceAsInputStream("req.json"))
    protected val expected: JaicpBotResponse get() = getResourceAsString("resp.json").asJsonHttpBotResponse().jaicp

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


    protected val HttpBotResponse.jaicp: JaicpBotResponse
        get() {
            val response = output.toString().asJaicpBotResponse().apply {
                processingTime = 0
                timestamp = 0
            }
            return response.copy(data = buildJsonObject {
                copyField(response.data, "answer")
                copyField(response.data, "replies")
                copyField(response.data, "dialer")
            })
        }
}

private fun JsonObjectBuilder.copyField(from: JsonElement, field: String) =
    from.jsonObject[field]?.let { put(field, it) }