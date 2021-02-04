package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import kotlin.reflect.full.primaryConstructor

abstract class PluginAdapter(val project: Project) {

    fun apply() = project.run {
        apply()
        gradle.projectsEvaluated { afterEvaluated() }
    }

    abstract fun Project.apply()

    open fun Project.afterEvaluated() {}
}

inline fun <reified P: PluginAdapter> apply(): Plugin<Project> = Plugin {
    P::class.primaryConstructor!!.call(it).apply()
}