package com.justai.jaicf.activator.llm.test.openai

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import org.opentest4j.TestAbortedException
import java.lang.reflect.Method
import java.util.Optional

class OpenAIExtension : InvocationInterceptor {

    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        val annotation = findAnnotation(extensionContext)
        if (annotation == null) {
            invocation.proceed()
            return
        }

        val apiKey = System.getenv("OPENAI_API_KEY")
        if (apiKey.isNullOrBlank()) {
            throw TestAbortedException("Skipping test '${extensionContext.displayName}': OPENAI_API_KEY is not set")
        }

        val maxAttempts = annotation.attempts

        for (attempt in 1..maxAttempts) {
            try {
                println("Running test '${extensionContext.displayName}' (attempt $attempt/$maxAttempts)")
                invocation.proceed()
                return
            } catch (e: Throwable) {
                if (attempt < maxAttempts) {
                    println("Retrying test '${extensionContext.displayName}' due to failure: ${e.message}")
                } else {
                    throw e
                }
            }
        }
    }

    private fun findAnnotation(context: ExtensionContext): OpenAITest? =
        context.testMethod
            .flatMap { method -> Optional.ofNullable(method.getAnnotation(OpenAITest::class.java)) }
            .orElseGet {
                context.testClass
                    .map { clazz -> clazz.getAnnotation(OpenAITest::class.java) }
                    .orElse(null)
            }
}