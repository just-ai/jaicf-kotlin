plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", Version.stdLib))

    implementation(project(":core"))
    implementation(project(":activators:llm"))

    implementation("org.slf4j:slf4j-simple" version {slf4j})
    implementation("org.slf4j:slf4j-log4j12" version {slf4j})
    implementation("org.jline:jline:3.30.4")

    testImplementation("org.junit.jupiter:junit-jupiter" version {jUnit})
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    test {
        useJUnitPlatform()
    }
    build {
        dependsOn(shadowJar)
    }
}