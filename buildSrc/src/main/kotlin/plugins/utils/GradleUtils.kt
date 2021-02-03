package plugins.utils

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

val Project.isRoot get() = parent == null

inline fun <reified P: Plugin<Project>> Project.applySafely(): Boolean {
    return if (plugins.hasPlugin(P::class.java)) {
        false
    } else {
        plugins.apply(P::class.java)
        true
    }
}

fun Project.localProperties(): Properties = Properties().apply {
    rootProject.file("local.properties").inputStream().use(::load)
}