package com.justai.jaicf.plugins.jaicpdeploy

import org.gradle.api.Plugin
import org.gradle.api.Project
import kotlin.reflect.KClass

internal inline fun <reified T: Any> Project.prop(
    name: String,
    mapper: (String) -> T? = { it as? T }
): T? = findProperty("$JAICP_PROPS_NAMESPACE.$name")?.let { mapper(it.toString()) }

internal fun Project.applyPluginSafely(klass: KClass<out Plugin<*>>): Boolean {
    if (plugins.hasPlugin(klass.java)) {
        return false
    }
    plugins.apply(klass.java)
    return true
}

internal val Project.isRootProject: Boolean get() = parent == null