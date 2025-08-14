plugins {
    kotlin("jvm")
    `jaicf-junit`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", Version.stdLib))

    implementation(project(":core"))
    implementation(project(":activators:llm"))

    implementation(libs.slf4j.simple)
    implementation(libs.slf4j.log4j12)
    implementation(libs.jline)

    testImplementation(testFixtures(project(":activators:llm")))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
}

tasks {
    test {
        useJUnitPlatform()
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}