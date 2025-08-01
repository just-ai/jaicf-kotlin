package com.justai.jaicf.activator.llm.test.openai

import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(OpenAIExtension::class)
@Timeout(value = 60, unit = TimeUnit.SECONDS)
annotation class OpenAITest(val attempts: Int = 1)