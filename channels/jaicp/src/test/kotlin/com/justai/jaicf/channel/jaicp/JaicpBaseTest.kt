package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asHttpBotRequest
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.JaicpBotResponse
import com.justai.jaicf.channel.jaicp.dto.JaicpResponseData
import com.justai.jaicf.helpers.logging.WithLogger
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.TestMethodOrder
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

@Suppress("MemberVisibilityCanBePrivate")
@TestMethodOrder(MethodOrderer.Alphanumeric::class)
open class JaicpBaseTest(
    private val useCommonResources: Boolean = false,
    private val ignoreSessionId: Boolean = true
) :
    WithLogger {
    protected lateinit var testName: String
    protected lateinit var testNumber: String
    protected lateinit var testPackage: String

    protected val requestFromResources: HttpBotRequest get() = HttpBotRequest(getResourceAsInputStream("req.json"))
    protected val responseFromResources: JaicpBotResponse get() = getResourceAsString("resp.json").asJsonHttpBotResponse().jaicp

    protected val commonRequestFactory = RequestFactory()
    protected var clientId = "test-client-id"
        private set

    @BeforeEach
    fun setUp(testInfo: TestInfo) {
        if (useCommonResources) {
            clientId = UUID.randomUUID().toString()
            return
        }
        testName = testInfo.displayName
        testNumber = testName.split(" ")[0]
        testPackage = testName.split(" ")[1]
    }

    private fun getResource(name: String): File {
        val path = when (useCommonResources) {
            true -> "$COMMON_RESOURCES_PATH/$name"
            false -> "$RESOURCES_PATH/$testPackage/$testNumber/$name"
        }
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
        const val COMMON_RESOURCES_PATH = "./src/test/resources/common"
        const val COMMON_REQUEST_PATH = "req.json"
    }

    protected inner class RequestFactory {
        fun query(query: String, additionalRequestData: Pair<String, JsonElement>? = null) =
            getResourceAsString(COMMON_REQUEST_PATH).asJaicpBotRequest()
                .withAdditionalData(additionalRequestData)
                .copy(query = query, event = null, channelUserId = clientId)
                .stringify()
                .asHttpBotRequest()


        fun event(event: String, additionalData: Pair<String, JsonElement>? = null) =
            getResourceAsString(COMMON_REQUEST_PATH).asJaicpBotRequest()
                .withAdditionalData(additionalData)
                .copy(query = null, event = event, channelUserId = clientId)
                .stringify()
                .asHttpBotRequest()

        private fun JaicpBotRequest.withAdditionalData(additionalRequestData: Pair<String, JsonElement>?): JaicpBotRequest {
            val requestData = additionalRequestData?.run {
                rawRequest.toMutableMap().apply {
                    put(additionalRequestData.first, additionalRequestData.second)
                }
            } ?: return this
            return copy(rawRequest = JSON.encodeToJsonElement(requestData).jsonObject)
        }
    }

    fun HttpBotResponse.answers(text: String) = apply {
        assertEquals(text, jaicp.responseData.answer)
    }

    fun HttpBotResponse.doesInterrupt() = apply {
        assertTrue(jaicp.responseData.bargeInInterrupt?.interrupt ?: false)
    }

    fun HttpBotResponse.failsInterrupt() = apply {
        assertFalse(jaicp.responseData.bargeInInterrupt?.interrupt ?: false)
    }

    private val JaicpBotResponse.responseData: JaicpResponseData
        get() = logger.info(JSON.encodeToString(JaicpBotResponse.serializer(), this)).let {
            JSON.decodeFromJsonElement(data)
        }

    internal val HttpBotResponse.responseData
        get() = jaicp.responseData

    internal fun JaicpResponseData.parseReplies() = TestJaicpRepliesParser.parse(replies)

    protected val HttpBotResponse.jaicp: JaicpBotResponse
        get() {
            val response = JSON.parseToJsonElement(output.toString()).jsonObject.toMutableMap().apply {
                // nullify timestamp and processing
                put("timestamp", JsonPrimitive(0))
                put("processingTime", JsonPrimitive(0))

                // sessionId is UUID and we skip it in some tests
                if (ignoreSessionId) {
                    put("data", JsonObject(requireNotNull(get("data")).jsonObject.toMutableMap().apply {
                        remove("sessionId")
                    }))
                }
            }
            return JSON.decodeFromJsonElement(JaicpBotResponse.serializer(), JsonObject(response))
        }
}

private fun JsonObjectBuilder.copyField(from: JsonElement, field: String) =
    from.jsonObject[field]?.let { put(field, it) }

private fun JsonObjectBuilder.ignoreField(from: JsonElement, field: String) =
    from.jsonObject[field]?.let { put(field, it) }