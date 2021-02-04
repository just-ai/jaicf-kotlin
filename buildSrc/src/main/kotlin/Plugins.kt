import org.gradle.plugin.use.PluginDependenciesSpec

private fun PluginDependenciesSpec.internal(plugin: String) = id("com.justai.jaicf.plugins.internal.$plugin")
val PluginDependenciesSpec.`jaicf-kotlin-serialization` get() = internal("serialization")
val PluginDependenciesSpec.`jaicf-kotlin` get() = internal("kotlin")
val PluginDependenciesSpec.`jaicf-publish` get() = internal("publish")
val PluginDependenciesSpec.`jaicf-github-release` get() = internal("github")
val PluginDependenciesSpec.`jaicf-junit` get() = internal("junit")
