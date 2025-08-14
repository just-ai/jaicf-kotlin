package com.justai.jaicf.activator.llm.tracing

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TracingTest {
    
    @Test
    fun `test LangSmith config from environment`() {
        // Test with environment variables
        val config = LangSmithConfig.fromEnvironment()
        assertNotNull(config)
        assertFalse(config.enabled) // Should be false without API key
        
        // Test with explicit creation
        val explicitConfig = LangSmithConfig.create("test-key", "test-project")
        assertTrue(explicitConfig.enabled)
        assertEquals("test-key", explicitConfig.apiKey)
        assertEquals("test-project", explicitConfig.project)
    }
    
    @Test
    fun `test OpenTelemetry config from environment`() {
        val config = OpenTelemetryConfig.fromEnvironment()
        assertNotNull(config)
        assertFalse(config.enabled) // Should be false without environment variables
        
        // Test with explicit creation
        val explicitConfig = OpenTelemetryConfig(
            enabled = true,
            endpoint = "http://localhost:4317",
            serviceName = "test-service"
        )
        assertTrue(explicitConfig.enabled)
        assertEquals("http://localhost:4317", explicitConfig.endpoint)
        assertEquals("test-service", explicitConfig.serviceName)
    }
    
    @Test
    fun `test tracing constants`() {
        assertEquals("LANGCHAIN_API_KEY", TracingConstants.ENV_LANGSMITH_TRACING)
        assertEquals("LANGCHAIN_API_KEY", TracingConstants.ENV_API_KEY)
        assertEquals("LANGCHAIN_PROJECT", TracingConstants.ENV_PROJECT)
        assertEquals("OTEL_TRACES_ENABLED", TracingConstants.ENV_OTEL_TRACES_ENABLED)
        assertEquals("langsmith", TracingConstants.TRACER_LANGSMITH)
        assertEquals("opentelemetry", TracingConstants.TRACER_OPENTELEMETRY)
        assertEquals("llm", TracingConstants.RUN_TYPE_LLM)
        assertEquals("tool", TracingConstants.RUN_TYPE_TOOL)
        assertEquals("chain", TracingConstants.RUN_TYPE_CHAIN)
    }
    
    @Test
    fun `test tracing manager initialization`() {
        val manager = TracingManager.get()
        assertNotNull(manager)
        
        // Test that manager can be retrieved multiple times (singleton)
        val manager2 = TracingManager.get()
        assertSame(manager, manager2)
    }
    
    @Test
    fun `test LangSmith client creation`() {
        val config = LangSmithConfig.create("test-key", "test-project")
        val client = LangSmithClient(config)
        assertNotNull(client)
    }
    
    @Test
    fun `test OpenTelemetry client creation`() {
        val config = OpenTelemetryConfig(enabled = true, endpoint = "http://localhost:4317")
        val client = OpenTelemetryClient(config)
        assertNotNull(client)
    }
}
