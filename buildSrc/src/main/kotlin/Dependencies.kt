import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project

fun DependencyHandlerScope.core() = "implementation"(project(":core"))
fun DependencyHandlerScope.ktor(module: String): String = "io.ktor:$module" version { ktor }
fun DependencyHandlerScope.jackson(): String = "com.fasterxml.jackson.module:jackson-module-kotlin" version { jackson }
fun DependencyHandlerScope.slf4j(module: String): String = "org.slf4j:$module" version { slf4j }
fun DependencyHandlerScope.`coroutines-core`(): String = "org.jetbrains.kotlinx:kotlinx-coroutines-core" version { coroutinesCore }
fun DependencyHandlerScope.kotlinx(module: String): String = "org.jetbrains.kotlinx:$module"
fun DependencyHandlerScope.`tomcat-servlet`(): String = "org.apache.tomcat:servlet-api" version { tomcatServletApi }
fun DependencyHandlerScope.okHttp(module: String): String = "com.squareup.okhttp3:$module" version { okHttp }