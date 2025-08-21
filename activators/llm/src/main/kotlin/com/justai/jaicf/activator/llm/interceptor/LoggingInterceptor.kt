package com.justai.jaicf.activator.llm.interceptor

import com.justai.jaicf.helpers.logging.WithLogger

private const val LOGGING_ENABLE = "LOGGING_ENABLE"

class LoggingInterceptor() : Interceptor, WithLogger {
    override val configName: String
        get() = LOGGING_ENABLE

    override fun <T> intercept(chain: Interceptor.Chain<T>): T {
        val ctx = chain.ctx
        logger.info("-> ${ctx.opName ?: "call"} props=${ctx.props}")
        val started = System.nanoTime()
        return try {
            val out = chain.proceed()
            val tookMs = (System.nanoTime() - started) / 1_000_000
            logger.info("<- ok ${ctx.opName ?: "call"} in ${tookMs}ms")
            out
        } catch (t: Throwable) {
            val tookMs = (System.nanoTime() - started) / 1_000_000
            logger.info("<- fail ${ctx.opName ?: "call"} in ${tookMs}ms: ${t.message}")
            throw t
        }
    }

    class Factory : InterceptorFactory, WithLogger {
        override val envFlag: String = LOGGING_ENABLE
        override fun create(env: Map<String, String?>): Interceptor {
            logger.info("LoggingInterceptor registered")
            return LoggingInterceptor()
        }
    }
}