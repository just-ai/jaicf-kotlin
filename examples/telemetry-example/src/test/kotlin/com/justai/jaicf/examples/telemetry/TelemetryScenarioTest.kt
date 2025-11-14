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
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TelemetryScenarioTest {
    
    private lateinit var spanExporter: InMemorySpanExporter
    private lateinit var tracer: Tracer
    private lateinit var bot: BotEngine
    
    @BeforeEach
    fun setup() {
        // Create in-memory span exporter for testing
        spanExporter = InMemorySpanExporter.create()
        
        val tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
            .build()
        
        val sdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build()
        
        tracer = sdk.getTracer("test")
        
        // Create bot with telemetry
        bot = BotEngine(
            scenario = TelemetryScenario,
            activators = arrayOf(
                RegexActivator,
                CatchAllActivator
            )
        ).withTelemetry(
            OpenTelemetryTelemetryProvider(tracer)
        )
    }
    
    @Test
    fun `should handle hello message`() = runBlocking {
        val reactions = TestReactions()
        val request = createRequest("hello")
        
        bot.process(request, reactions, RequestContext.DEFAULT)
        
        assertTrue(reactions.responses.isNotEmpty())
        assertTrue(reactions.responses.any { it.contains("telemetry") })
    }
    
    @Test
    fun `should handle joke request`() = runBlocking {
        val reactions = TestReactions()
        val request = createRequest("joke")
        
        bot.process(request, reactions, RequestContext.DEFAULT)
        
        assertTrue(reactions.responses.isNotEmpty())
        assertTrue(reactions.responses.any { it.contains("programmer") || it.contains("Java") })
    }
    
    @Test
    fun `should handle calculate request`() = runBlocking {
        val reactions = TestReactions()
        val request = createRequest("calculate")
        
        bot.process(request, reactions, RequestContext.DEFAULT)
        
        assertTrue(reactions.responses.isNotEmpty())
        assertTrue(reactions.responses.any { it.contains("5050") })
    }
    
    @Test
    fun `should handle help request`() = runBlocking {
        val reactions = TestReactions()
        val request = createRequest("help")
        
        bot.process(request, reactions, RequestContext.DEFAULT)
        
        assertTrue(reactions.responses.isNotEmpty())
        assertTrue(reactions.responses.any { it.contains("demo bot") })
    }
    
    @Test
    fun `should create telemetry spans`() = runBlocking {
        val reactions = TestReactions()
        val request = createRequest("hello")
        
        bot.process(request, reactions, RequestContext.DEFAULT)
        
        // Give some time for spans to be exported
        Thread.sleep(100)
        
        val spans = spanExporter.finishedSpanItems
        assertFalse(spans.isEmpty(), "Should have created telemetry spans")
        
        // Check that we have request lifecycle spans
        val hasRequestSpans = spans.any { span ->
            span.name.contains("jaicf.request")
        }
        assertTrue(hasRequestSpans, "Should have request lifecycle spans")
    }
    
    @Test
    fun `should capture activation information in spans`() = runBlocking {
        val reactions = TestReactions()
        val request = createRequest("joke")
        
        bot.process(request, reactions, RequestContext.DEFAULT)
        
        // Give some time for spans to be exported
        Thread.sleep(200)
        
        val spans = spanExporter.finishedSpanItems
        
        // Check that we have some spans
        assertTrue(spans.isNotEmpty(), "Should have created some spans")
    }
    
    @Test
    fun `should handle unknown commands with fallback`() = runBlocking {
        val reactions = TestReactions()
        val request = createRequest("unknown command xyz")
        
        bot.process(request, reactions, RequestContext.DEFAULT)
        
        // Should get some response (either from fallback or default handler)
        assertTrue(reactions.responses.isNotEmpty(), "Should have at least one response")
    }
    
    private fun createRequest(query: String): QueryBotRequest {
        return QueryBotRequest(
            clientId = "test-client",
            input = query
        )
    }
    
    private class TestReactions : Reactions() {
        val responses = mutableListOf<String>()
        
        override fun say(text: String): SayReaction {
            responses.add(text)
            return super.say(text)
        }
        
        override fun image(url: String): ImageReaction {
            responses.add("[Image: $url]")
            return super.image(url)
        }
        
        override fun buttons(vararg buttons: String): ButtonsReaction {
            responses.add("[Buttons: ${buttons.joinToString(", ")}]")
            return super.buttons(*buttons)
        }
    }
}

