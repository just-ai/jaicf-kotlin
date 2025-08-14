import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin LLM Activator Adapter"
ext[POM_DESCRIPTION] = "JAICF-Kotlin LLM Activator Adapter."

plugins {
    `jaicf-kotlin`
    `jaicf-kotlin-serialization`
    `jaicf-publish`
    `jaicf-junit`
}

repositories {
    mavenCentral()
}

dependencies {
    core()
    api(okHttp("okhttp"))
    api(okHttp("logging-interceptor"))
    api(`coroutines-core`())
    api("com.openai:openai-java:2.11.0") {
        exclude("com.squareup.okhttp3", "okhttp")
        exclude("com.squareup.okhttp3", "logging-interceptor")
    }
    implementation(jackson())
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:${Version.jackson}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Version.jackson}")
    implementation("io.modelcontextprotocol:kotlin-sdk:0.5.0")

    api("io.ktor:ktor-client:3.0.2")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.2")
    implementation("io.ktor:ktor-serialization-jackson:3.0.2")

    // LangSmith tracing dependencies
    implementation("dev.langchain4j:langchain4j:1.0.0")
    implementation("dev.langchain4j:langchain4j-open-ai:1.0.0")
    implementation("dev.langchain4j:langchain4j-core:1.0.0")
    
    // OpenTelemetry tracing dependencies
    implementation("io.opentelemetry:opentelemetry-api:1.30.1")
    implementation("io.opentelemetry:opentelemetry-sdk:1.30.1")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.30.1")
    implementation("io.opentelemetry:opentelemetry-semconv:1.30.1-alpha")

    testImplementation("io.mockk:mockk" version { mockk })
    testImplementation(ktor("ktor-client-mock"))

    implementation("org.junit.jupiter:junit-jupiter-api" version { jUnit })
}
