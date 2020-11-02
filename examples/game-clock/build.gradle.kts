plugins {
    kotlin("jvm") version Version.kotlin
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", Version.stdLib))

    implementation(project(":core"))
    implementation(project(":channels:jaicp"))
    implementation(project(":channels:alexa"))
    implementation(project(":channels:google-actions"))

    implementation("io.ktor:ktor-server-netty" version {ktor})

    implementation("org.slf4j:slf4j-simple" version {slf4j})
    implementation("org.slf4j:slf4j-log4j12" version {slf4j})

    testImplementation("org.junit.jupiter:junit-jupiter-api" version {jUnit})
    testRuntime("org.junit.jupiter:junit-jupiter-engine" version {jUnit})
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    test {
        useJUnitPlatform()
    }
}