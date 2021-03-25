//package plugins.publish
//
//import com.jfrog.bintray.gradle.BintrayExtension
//import com.jfrog.bintray.gradle.BintrayPlugin
//import org.gradle.api.Plugin
//import org.gradle.api.Project
//import org.gradle.api.plugins.JavaPlugin
//import org.gradle.api.publish.PublishingExtension
//import org.gradle.api.publish.maven.MavenPublication
//import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
//import org.gradle.api.tasks.SourceSetContainer
//import org.gradle.api.tasks.bundling.Jar
//import org.gradle.kotlin.dsl.*
//import org.jetbrains.dokka.gradle.*
//import plugins.*
//import plugins.utils.*
//
//
//class JaicfPublishPlugin : Plugin<Project> by apply<CentralPublish>()
//
//class JaicfPublish(project: Project) : PluginAdapter(project) {
//    private val properties by lazy { loadLocalProperties() }
//
//    private val bintrayUser by lazy { properties.getProperty("bintray.user") }
//    private val bintrayKey by lazy { properties.getProperty("bintray.apikey") }
//    private val bintrayRepo = "jaicf"
//    private val bintrayOrg = "just-ai"
//    private val bintrayName by lazy { project.name }
//    private val siteUrl = "https://framework.just-ai.com"
//    private val gitUrl = "https://github.com/just-ai"
//    private val allLicenses = arrayOf("Apache-2.0")
//
//    override fun Project.apply() {
//        applySafely<JavaPlugin>()
//        applySafely<MavenPublishPlugin>()
//        applySafely<DokkaPlugin>()
//
//        afterEvaluate {
//            val dokkaJavadoc = tasks.register<DokkaTask>("dokkaJavadoc") {
//                outputFormat = "javadoc"
//                outputDirectory = "$buildDir/javadoc"
//                configuration.noStdlibLink = true
//                configuration.noJdkLink = true
//            }
//
//            val sourcesJar = tasks.register<Jar>("sourcesJar") {
//                val allSource =
//                    project.extensions.getByName<SourceSetContainer>("sourceSets").getByName("main").allSource
//                archiveClassifier.set("sources")
//                from(allSource)
//            }
//
//            val javadocJar = tasks.register<Jar>("javadocJar") {
//                archiveClassifier.set("javadoc")
//                from(dokkaJavadoc)
//                dependsOn(dokkaJavadoc)
//            }
//
//            configurePublish(sourcesJar, javadocJar)
//        }
//
//        configureBintray()
//    }
//
//    private fun Project.configurePublish(sources: Any, javadoc: Any) {
//        configure<PublishingExtension> {
//            publications {
//                create<MavenPublication>(name) {
//                    from(components["java"])
//                    val pomName = project.extra.properties[POM_NAME] as? String ?: error("No pomName defined")
//                    val pomDescription =
//                        project.extra.properties[POM_DESCRIPTION] as? String ?: error("No pomDescription defined")
//
//                    pom {
//                        name.set(pomName)
//                        description.set(pomDescription)
//                        url.set("https://framework.just-ai.com")
//
//                        licenses {
//                            license {
//                                name.set("The Apache License, Version 2.0")
//                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
//                            }
//                        }
//
//                        developers {
//                            developer {
//                                id.set("morfeusys")
//                                email.set("che@just-ai.com")
//                            }
//                            developer {
//                                id.set("denire")
//                                email.set("v.metelyagin@just-ai.com")
//                            }
//                            developer {
//                                id.set("nikvoloshin")
//                                email.set("n.voloshin@just-ai.com")
//                            }
//                        }
//
//                        scm {
//                            connection.set("scm:git:git@github.com:just-ai/jaicf-kotlin.git")
//                            developerConnection.set("scm:git:git@github.com:just-ai/jaicf-kotlin.git")
//                            url.set("https://github.com/just-ai/jaicf-kotlin")
//                        }
//                    }
//
//                    artifact(sources)
//                    artifact(javadoc)
//                }
//            }
//        }
//    }
//
//    private fun Project.configureBintray() {
//        afterEvaluate {
//            tasks.named("bintrayUpload") {
//                dependsOn("assemble", "sourcesJar", "javadocJar")
//            }
//
//            configure<BintrayExtension> {
//                user = bintrayUser
//                key = bintrayKey
//
//                setPublications(this@JaicfPublish.project.name)
//
//                pkg.apply {
//                    userOrg = bintrayOrg
//                    repo = bintrayRepo
//                    name = bintrayName
//
//                    websiteUrl = siteUrl
//                    vcsUrl = gitUrl
//                    setLicenses(*allLicenses)
//
//                    publish = true
//                    override = true
//                    publicDownloadNumbers = true
//
//                    version.gpg.sign = false
//                }
//            }
//
//        }
//
//        applySafely<BintrayPlugin>()
//    }
//}
