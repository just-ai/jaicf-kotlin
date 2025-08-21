package com.justai.jaicf.activator.llm.wrapper

import com.justai.jaicf.activator.llm.LLMProps
import com.justai.jaicf.activator.llm.interceptor.Interceptor
import com.justai.jaicf.activator.llm.interceptor.InterceptorConfigLoader
import com.openai.client.OpenAIClient
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.function.Consumer

typealias ClientCall<T> = (OpenAIClient) -> T

interface ProcessingOpenAIClient : OpenAIClient {
    fun <T> process(opName: String? = null, block: ClientCall<T>): T
}

data class CallContext(
    val props: LLMProps,
    val opName: String? = null
)

class OpenAIClientBuilder(
    private val openAIClient: OpenAIClient,
    private val props: LLMProps,
    private val interceptors: List<Interceptor> = emptyList()
) {
    fun build(): ProcessingOpenAIClient {
        val chain = interceptors + InterceptorConfigLoader.fromEnv()
        return ProxyProcessingOpenAIClient(openAIClient, props, chain)
    }
}

private class ProxyProcessingOpenAIClient(
    private val base: OpenAIClient,
    private val props: LLMProps,
    private val interceptors: List<Interceptor>
) : ProcessingOpenAIClient,
    OpenAIClient by newClientProxy(base, props, interceptors) {

    override fun <T> process(opName: String?, block: ClientCall<T>): T {
        val ctx = CallContext(props, opName)
        val chain = RealChain(interceptors, 0, base, ctx, block)
        return chain.proceed()
    }

    companion object {

        private fun newClientProxy(
            target: OpenAIClient,
            props: LLMProps,
            interceptors: List<Interceptor>
        ): OpenAIClient {
            val excluded = setOf("async", "close", "withRawResponse")

            val handler = InvocationHandler { _, method, args ->
                routeCall(target, props, interceptors, excluded, method, args)
            }

            return Proxy.newProxyInstance(
                OpenAIClient::class.java.classLoader,
                arrayOf(OpenAIClient::class.java),
                handler
            ) as OpenAIClient
        }

        private fun routeCall(
            base: OpenAIClient,
            props: LLMProps,
            interceptors: List<Interceptor>,
            excluded: Set<String>,
            method: Method,
            args: Array<out Any?>?
        ): Any? {
            val a = args ?: emptyArray()
            if (method.name in excluded) {
                return method.invoke(base, *a)
            }

            if (method.name == "withOptions" &&
                method.parameterTypes.contentEquals(arrayOf(Consumer::class.java))
            ) {
                val next = method.invoke(base, *a) as OpenAIClient
                return newClientProxy(next, props, interceptors)
            }

            val ctx = CallContext(props, method.name)
            val chain = RealChain(interceptors, 0, base, ctx) { client ->
                method.invoke(client, *a)
            }
            val result = chain.proceed()

            return if (result is OpenAIClient) {
                newClientProxy(result, props, interceptors)
            } else {
                result
            }
        }
    }
}

private class RealChain<T>(
    private val interceptors: List<Interceptor>,
    private val index: Int,
    private val baseClient: OpenAIClient,
    override val ctx: CallContext,
    private val originalCall: ClientCall<T>
) : Interceptor.Chain<T> {

    override fun proceed(): T {
        return if (index < interceptors.size) {
            val next = RealChain(interceptors, index + 1, baseClient, ctx, originalCall)
            interceptors[index].intercept(next)
        } else {
            originalCall(baseClient)
        }
    }
}