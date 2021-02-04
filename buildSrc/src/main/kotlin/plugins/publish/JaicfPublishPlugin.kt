package plugins.publish

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import org.jetbrains.dokka.gradle.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import plugins.*
import plugins.utils.*

class JaicfPublishPlugin : Plugin<Project> by apply<JaicfPublish>()

class JaicfPublish(project: Project) : PluginAdapter(project) {
    private val properties by lazy { loadLocalProperties() }

    private val bintrayUser by lazy { properties.getProperty("bintray.user") }
    private val bintrayKey by lazy { properties.getProperty("bintray.apikey") }
    private val bintrayRepo = "jaicf"
    private val bintrayOrg = "just-ai"
    private val bintrayName by lazy { project.name }
    private val siteUrl = "https://framework.just-ai.com"
    private val gitUrl = "https://github.com/just-ai"
    private val allLicenses = arrayOf("Apache-2.0")

    override fun Project.apply() {
        applySafely<JavaPlugin>()
        applySafely<MavenPublishPlugin>()
        applySafely<DokkaPlugin>()

        afterEvaluate {
            val dokkaJavadoc = tasks.register<DokkaTask>("dokkaJavadoc") {
                outputFormat = "javadoc"
                outputDirectory = "$buildDir/javadoc"
                configuration.noStdlibLink = true
                configuration.noJdkLink = true
            }

            val sourcesJar = tasks.register<Jar>("sourcesJar") {
                val allSource = project.extensions.getByName<SourceSetContainer>("sourceSets").getByName("main").allSource
                archiveClassifier.set("sources")
                from(allSource)
            }

            val javadocJar = tasks.register<Jar>("javadocJar") {
                archiveClassifier.set("javadoc")
                from(dokkaJavadoc)
                dependsOn(dokkaJavadoc)
            }

            configurePublish(sourcesJar, javadocJar)
        }

        configureBintray()
    }

    private fun Project.configurePublish(sources: Any, javadoc: Any) {
        configure<PublishingExtension> {
            publications {
                create<MavenPublication>(name) {
                    from(components["java"])

                    artifact(sources)
                    artifact(javadoc)
                }
            }
        }
    }

    private fun Project.configureBintray() {
        afterEvaluate {
            tasks.named("bintrayUpload") {
                dependsOn("assemble", "sourcesJar", "javadocJar")
            }

            configure<BintrayExtension> {
                user = bintrayUser
                key = bintrayKey

                setPublications(this@JaicfPublish.project.name)

                pkg.apply {
                    userOrg = bintrayOrg
                    repo = bintrayRepo
                    name = bintrayName

                    websiteUrl = siteUrl
                    vcsUrl = gitUrl
                    setLicenses(*allLicenses)

                    publish = true
                    override = true
                    publicDownloadNumbers = true

                    version.gpg.sign = false
                }
            }

        }

        applySafely<BintrayPlugin>()
    }
}
