package com.justai.jaicf.activator.llm.tracing

import org.slf4j.LoggerFactory

/**
 * Manages multiple tracers (LangSmith, OpenTelemetry)
 */
class TracingManager private constructor() {
    
    companion object {
        private val logger = LoggerFactory.getLogger(TracingManager::class.java)
        private var instance: TracingManager? = null
        
        fun get(): TracingManager {
            if (instance == null) {
                instance = TracingManager()
            }
            return instance!!
        }
        
        fun initFromEnvironment(): TracingManager {
            val manager = get()
            
            // Initialize LangSmith tracer if enabled
            if (System.getenv("LANGSMITH_API_KEY") != null) {
                val langSmithTracer = LangSmithTracer.init()
                manager.addTracer(langSmithTracer)
                logger.info("LangSmith tracer initialized")
            }
            
            // Initialize OpenTelemetry tracer if enabled
            val oTelConfig = OpenTelemetryConfig.fromEnvironment()
            if (oTelConfig.enabled) {
                val oTelTracer = OpenTelemetryTracer.init(oTelConfig)
                manager.addTracer(oTelTracer)
                logger.info("OpenTelemetry tracer initialized")
            }
            
            return manager
        }
    }
    
    private val tracers = mutableListOf<Tracer>()
    
    fun addTracer(tracer: Tracer) {
        tracers.add(tracer)
    }
    
    fun startLLMRun(
        context: com.justai.jaicf.context.BotContext,
        request: com.justai.jaicf.api.BotRequest,
        props: com.justai.jaicf.activator.llm.LLMProps,
        messages: List<Map<String, Any>>
    ): Map<String, String> {
        val runIds = mutableMapOf<String, String>()
        
        tracers.forEach { tracer ->
            if (tracer.isEnabled) {
                try {
                    val runId = tracer.startLLMRun(context, request, props, messages)
                    if (runId.isNotEmpty()) {
                        runIds[tracer.name] = runId
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to start LLM run with tracer ${tracer.name}: ${e.message}")
                }
            }
        }
        
        return runIds
    }
    
    fun endLLMRun(
        runIds: Map<String, String>,
        completion: com.openai.models.chat.completions.ChatCompletion,
        usage: com.openai.models.completions.CompletionUsage?
    ) {
        runIds.forEach { (tracerName, runId) ->
            val tracer = tracers.find { it.name == tracerName }
            if (tracer != null && tracer.isEnabled) {
                try {
                    tracer.endLLMRun(runId, completion, usage)
                } catch (e: Exception) {
                    logger.warn("Failed to end LLM run with tracer $tracerName: ${e.message}")
                }
            }
        }
    }
    
    fun startToolRun(
        parentRunIds: Map<String, String>,
        toolCall: com.openai.models.chat.completions.ChatCompletionMessageToolCall,
        arguments: Any?
    ): Map<String, String> {
        val toolRunIds = mutableMapOf<String, String>()
        
        parentRunIds.forEach { (tracerName, parentRunId) ->
            val tracer = tracers.find { it.name == tracerName }
            if (tracer != null && tracer.isEnabled) {
                try {
                    val runId = tracer.startToolRun(parentRunId, toolCall, arguments)
                    if (runId.isNotEmpty()) {
                        toolRunIds[tracerName] = runId
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to start tool run with tracer $tracerName: ${e.message}")
                }
            }
        }
        
        return toolRunIds
    }
    
    fun endToolRun(
        toolRunIds: Map<String, String>,
        result: com.justai.jaicf.activator.llm.tool.LLMToolResult
    ) {
        toolRunIds.forEach { (tracerName, runId) ->
            val tracer = tracers.find { it.name == tracerName }
            if (tracer != null && tracer.isEnabled) {
                try {
                    tracer.endToolRun(runId, result)
                } catch (e: Exception) {
                    logger.warn("Failed to end tool run with tracer $tracerName: ${e.message}")
                }
            }
        }
    }
    
    fun startChainRun(
        context: com.justai.jaicf.context.BotContext,
        request: com.justai.jaicf.api.BotRequest,
        name: String
    ): Map<String, String> {
        val chainRunIds = mutableMapOf<String, String>()
        
        tracers.forEach { tracer ->
            if (tracer.isEnabled) {
                try {
                    val runId = tracer.startChainRun(context, request, name)
                    if (runId.isNotEmpty()) {
                        chainRunIds[tracer.name] = runId
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to start chain run with tracer ${tracer.name}: ${e.message}")
                }
            }
        }
        
        return chainRunIds
    }
    
    fun endChainRun(
        runIds: Map<String, String>,
        outputs: Map<String, Any>
    ) {
        runIds.forEach { (tracerName, runId) ->
            val tracer = tracers.find { it.name == tracerName }
            if (tracer != null && tracer.isEnabled) {
                try {
                    tracer.endChainRun(runId, outputs)
                } catch (e: Exception) {
                    logger.warn("Failed to end chain run with tracer $tracerName: ${e.message}")
                }
            }
        }
    }

    /**
     * Start a test chain run that will contain all LLM calls in a test
     */
    fun startTestChainRun(
        context: com.justai.jaicf.context.BotContext,
        request: com.justai.jaicf.api.BotRequest,
        testName: String
    ): Map<String, String> {
        val runIds = mutableMapOf<String, String>()
        
        tracers.forEach { tracer ->
            if (tracer.isEnabled) {
                try {
                    val runId = tracer.startTestChainRun(context, request, testName)
                    if (runId.isNotEmpty()) {
                        runIds[tracer.name] = runId
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to start test chain run with tracer ${tracer.name}: ${e.message}")
                }
            }
        }
        
        return runIds
    }

    /**
     * End a test chain run
     */
    fun endTestChainRun(
        runIds: Map<String, String>,
        outputs: Map<String, Any>
    ) {
        runIds.forEach { (tracerName, runId) ->
            val tracer = tracers.find { it.name == tracerName }
            if (tracer != null && tracer.isEnabled) {
                try {
                    tracer.endTestChainRun(runId, outputs)
                } catch (e: Exception) {
                    logger.warn("Failed to end test chain run with tracer $tracerName: ${e.message}")
                }
            }
        }
    }
}
