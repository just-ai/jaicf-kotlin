package com.justai.jaicf.activator.llm.interceptor

import com.justai.jaicf.activator.llm.CallContext
import com.justai.jaicf.helpers.logging.WithLogger

private const val TRACING_ENABLE = "TRACING_ENABLE"

class TracingInterceptor(
    private val onStart: (CallContext) -> Unit = {},
    private val onEnd: (CallContext, Result<Any?>) -> Unit = { _, _ -> },
) : Interceptor {

    override val configName: String
        get() = TRACING_ENABLE

    @Suppress("UNCHECKED_CAST")
    override fun <T> intercept(chain: Interceptor.Chain<T>): T {
        onStart(chain.ctx)
        val result = runCatching { chain.proceed() }
        onEnd(chain.ctx, result.map { it as Any? })
        return result.getOrThrow()
    }

    class Factory : InterceptorFactory, WithLogger {
        override val envFlag: String = TRACING_ENABLE
        override fun create(env: Map<String, String?>): Interceptor {
            logger.info("TracingInterceptor registered")
            return TracingInterceptor()
        }
    }
}