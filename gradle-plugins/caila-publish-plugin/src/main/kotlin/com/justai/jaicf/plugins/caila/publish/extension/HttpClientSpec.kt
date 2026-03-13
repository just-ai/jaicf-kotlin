package com.justai.jaicf.plugins.caila.publish.extension

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class HttpClientSpec @Inject constructor(
    objectFactory: ObjectFactory
) {
    @get:Input
    val logLevel: Property<String> = objectFactory.property<String>().convention("ALL")

    @get:Input
    val connectTimeoutMs: Property<Int> = objectFactory.property<Int>().convention(10_000)

    @get:Input
    val requestTimeoutMs: Property<Int> = objectFactory.property<Int>().convention(35_000)

    @get:Input
    val keepAliveTimeMs: Property<Int> = objectFactory.property<Int>().convention(35_000)
}