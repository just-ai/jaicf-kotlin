plugins {
    kotlin("jvm")
    id("com.justai.jaicf.plugins.internal.publish")
}

dependencies {
    implementation(kotlin("stdlib", Version.stdLib))

    api("org.slf4j:slf4j-api" version {slf4j})

    implementation("org.apache.tomcat:servlet-api" version { tomcatServletApi })
    implementation("io.ktor:ktor-server-core" version {ktor})
    implementation("org.junit.jupiter:junit-jupiter-api" version {jUnit})

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
