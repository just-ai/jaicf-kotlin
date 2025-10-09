import org.gradle.plugin.use.PluginDependenciesSpec

private fun PluginDependenciesSpec.internal(plugin: String) = id("com.justai.jaicf.plugins.internal.$plugin")

val PluginDependenciesSpec.`jaicf-publish` get() = internal("publish")
val PluginDependenciesSpec.`jaicf-github-release` get() = internal("github")