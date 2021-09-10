package plugins.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.DokkaTask
import plugins.PluginAdapter
import plugins.apply
import plugins.utils.applySafely
import plugins.utils.loadLocalProperties
import java.io.File
import java.net.URI

const val POM_NAME = "pomName"
const val POM_DESCRIPTION = "pomDescription"

private const val SONATYPE_USER = "sonatype.user"
private const val SONATYPE_PASSWORD = "sonatype.password"
private const val SIGNING_KEY = "signing.keyId"
private const val SIGNING_PASS = "signing.password"
private const val SECRING_FILE = "signing.secretKeyRingFile"

private const val RELEASE_REPO = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
private const val SNAPSHOTS_REPO = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

private const val MAVEN_CENTRAL = "MavenCentral"

class CentralPublishPlugin : Plugin<Project> by apply<CentralPublish>()

@Suppress("DuplicatedCode", "UnstableApiUsage")
class CentralPublish(project: Project) : PluginAdapter(project) {
    private val properties by lazy { loadLocalProperties() }
    private val secKey by lazy { properties.getProperty(SIGNING_KEY) }
    private val secPass by lazy { properties.getProperty(SIGNING_PASS) }
    private val secRing by lazy { properties.getProperty(SECRING_FILE) }
    private val sonatypeUser by lazy { properties.getProperty(SONATYPE_USER) }
    private val sonatypePassword by lazy { properties.getProperty(SONATYPE_PASSWORD) }

    override fun Project.apply() {
        applySafely<JavaPlugin>()
        applySafely<MavenPublishPlugin>()
        applySafely<DokkaPlugin>()
        applySafely<SigningPlugin>()

        afterEvaluate {
            val dokkaJavadoc = tasks.register<DokkaTask>("dokkaJavadoc") {
                outputFormat = "javadoc"
                outputDirectory = "$buildDir/javadoc"
                configuration.noStdlibLink = true
                configuration.noJdkLink = true
            }

            val sourcesJar = tasks.register<Jar>("sourcesJar") {
                val allSource = project.extensions
                    .getByName<SourceSetContainer>("sourceSets")
                    .getByName("main").allSource
                archiveClassifier.set("sources")
                from(allSource)
            }

            val javadocJar = tasks.register<Jar>("javadocJar") {
                archiveClassifier.set("javadoc")
                from(dokkaJavadoc)
                dependsOn(dokkaJavadoc)
            }

            configurePublication(sourcesJar, javadocJar)
        }
    }

    private fun Project.configurePublication(sources: Any, javadoc: Any) {
        configure<PublishingExtension> {
            val isSnapshot = (project.version as String).endsWith("SNAPSHOT")

            repositories {
                maven {
                    name = MAVEN_CENTRAL
                    url = when (isSnapshot) {
                        true -> URI(SNAPSHOTS_REPO)
                        false -> URI(RELEASE_REPO)
                    }
                    credentials {
                        username = sonatypeUser
                        password = sonatypePassword
                    }
                }
            }

            publications {
                create<MavenPublication>(name) {
                    from(components["java"])
                    val pomName = extraProperty(POM_NAME)
                    val pomDescription = extraProperty(POM_DESCRIPTION)

                    configurePom(pomName, pomDescription)

                    artifact(sources)
                    artifact(javadoc)
                }
            }

            if (isMavenCentralPublication && !isSnapshot) {
                configure<SigningExtension> {
                    useInMemoryPgpKeys(secKey, File(secRing).readText(), secPass)
                    sign(publications)
                }
            }
        }
    }
}

private val Project.isMavenCentralPublication: Boolean
    get() {
        val task = project.gradle.startParameter.taskNames.firstOrNull()
        val isPublish = task?.endsWith("publish") ?: false
        val isByCentralPublishTask = task?.endsWith("${MAVEN_CENTRAL}Repository") ?: false
        return isPublish || isByCentralPublishTask
    }

private fun Project.extraProperty(name: String) =
    project.extra.properties[name] as? String ?: error("No $name defined")

@Suppress("UnstableApiUsage", "DuplicatedCode")
private fun MavenPublication.configurePom(
    pomName: String,
    pomDescription: String
) {
    pom {
        name.set(pomName)
        description.set(pomDescription)
        url.set("https://framework.just-ai.com")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("morfeusys")
                email.set("che@just-ai.com")
            }
            developer {
                id.set("denire")
                email.set("v.metelyagin@just-ai.com")
            }
            developer {
                id.set("nikvoloshin")
                email.set("n.voloshin@just-ai.com")
            }
        }

        scm {
            connection.set("scm:git:git@github.com:just-ai/jaicf-kotlin.git")
            developerConnection.set("scm:git:git@github.com:just-ai/jaicf-kotlin.git")
            url.set("https://github.com/just-ai/jaicf-kotlin")
        }
    }
}