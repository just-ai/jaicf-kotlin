plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.justai.jaicf.examples.mrhappy.channel.ConsoleKt")
}

dependencies {
    implementation(kotlin("stdlib", Version.stdLib))
    implementation(project(":core"))
    implementation("org.slf4j:slf4j-simple" version { slf4j })
    implementation("com.fasterxml.jackson.core:jackson-databind" version { jackson })
}

tasks {
    compileKotlin { kotlinOptions.jvmTarget = "1.8" }
    compileTestKotlin { kotlinOptions.jvmTarget = "1.8" }
}
