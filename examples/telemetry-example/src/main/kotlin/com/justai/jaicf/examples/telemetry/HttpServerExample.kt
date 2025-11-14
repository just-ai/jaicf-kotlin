package com.justai.jaicf.examples.telemetry

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.catchall.CatchAllActivator
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.telemetry.opentelemetry.OpenTelemetryTelemetryProvider
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * HTTP server example with OpenTelemetry integration
 */
fun main() {
    val tracer = try {
        println("Attempting to connect to OTLP endpoint at http://localhost:4317...")
        TelemetryConfig.buildTracerWithOtlp()
    } catch (e: Exception) {
        println("Could not connect to OTLP endpoint, using console logging instead")
        TelemetryConfig.buildTracerWithLogging()
    }

    val bot = BotEngine(
        scenario = TelemetryScenario,
        activators = arrayOf(
            RegexActivator,
            CatchAllActivator
        )
    ).withTelemetry(
        OpenTelemetryTelemetryProvider(tracer)
    )

    embeddedServer(Netty, port = 8081) {
        routing {
            get("/") {
                call.respondText(
                    """
                    JAICF Telemetry Example Server
                    
                    Send POST request to /bot with JSON body:
                    {
                        "query": "your message",
                        "clientId": "optional-client-id"
                    }
                    
                    Example:
                    curl -X POST http://localhost:8080/bot \
                      -H "Content-Type: application/json" \
                      -d '{"query": "hello"}'
                    """.trimIndent(),
                    ContentType.Text.Plain
                )
            }

            post("/bot") {
                try {
                    val json = call.receiveText()
                    val jsonObj = Json.parseToJsonElement(json).jsonObject
                    val query = jsonObj["query"]?.jsonPrimitive?.content ?: ""
                    val clientId = jsonObj["clientId"]?.jsonPrimitive?.content ?: "anonymous"

                    val request = QueryBotRequest(clientId = clientId, input = query)
                    val reactions = HttpReactions()

                    bot.process(request, reactions, RequestContext.DEFAULT)

                    call.respond(
                        HttpStatusCode.OK,
                        reactions.toJson()
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }

            get("/health") {
                call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
            }
        }
    }.start(wait = true)
}

/**
 * HTTP-specific reactions implementation
 */
private class HttpReactions : Reactions() {
    private val responses = mutableListOf<Map<String, Any>>()

    override fun say(text: String): SayReaction {
        responses.add(mapOf("type" to "text", "text" to text))
        return super.say(text)
    }

    override fun image(url: String): ImageReaction {
        responses.add(mapOf("type" to "image", "url" to url))
        return super.image(url)
    }

    override fun buttons(vararg buttons: String): ButtonsReaction {
        responses.add(
            mapOf(
                "type" to "buttons",
                "buttons" to buttons.toList()
            )
        )
        return super.buttons(*buttons)
    }

    fun toJson(): Map<String, Any> {
        return mapOf("responses" to responses)
    }
}

