package com.justai.jaicf.plugins.caila.publish.extension

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.mapProperty
import javax.inject.Inject

/**
 * Specification for environment variables to be passed to the model container.
 * Provides a type-safe DSL for configuring environment variables.
 *
 * Example usage:
 * ```
 * environmentVariables {
 *     put("API_KEY", "secret123")
 *     put("LOG_LEVEL", "INFO")
 *     putAll(mapOf("VAR1" to "value1", "VAR2" to "value2"))
 * }
 * ```
 */
open class EnvVariablesSpec
@Inject
constructor(
    private val objectFactory: ObjectFactory,
) {
    @get:Input
    @get:Optional
    val variables: MapProperty<String, String> = objectFactory.mapProperty()

    /**
     * Add a single environment variable.
     *
     * @param key The environment variable name
     * @param value The environment variable value
     */
    fun put(key: String, value: String) {
        val current = variables.orNull?.toMutableMap() ?: mutableMapOf()
        current[key] = value
        variables.set(current)
    }

    /**
     * Add multiple environment variables from a map.
     *
     * @param vars Map of environment variable names to values
     */
    fun putAll(vars: Map<String, String>) {
        val current = variables.orNull?.toMutableMap() ?: mutableMapOf()
        current.putAll(vars)
        variables.set(current)
    }

    /**
     * Convert environment variables to the format expected by CAILA API.
     * Format: KEY=VALUE\nKEY2=VALUE2
     *
     * @return Environment variables string, or null if no variables are set
     */
    internal fun toEnvString(): String? {
        val vars = variables.orNull
        return if (vars.isNullOrEmpty()) {
            null
        } else {
            vars.entries.joinToString("\n") { "${it.key}=${it.value}" }
        }
    }
}