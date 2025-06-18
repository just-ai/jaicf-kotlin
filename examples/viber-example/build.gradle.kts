plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", Version.stdLib))

    implementation(project(":core"))
    implementation(project(":channels:jaicp"))
    implementation(project(":channels:viber"))

    implementation("org.slf4j:slf4j-simple" version { slf4j })
    implementation("org.slf4j:slf4j-log4j12" version { slf4j })
    implementation("io.ktor:ktor-server-netty" version { ktor })
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}
