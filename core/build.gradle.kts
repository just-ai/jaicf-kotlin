plugins {
    `jaicf-kotlin`
    `jaicf-publish`
    `jaicf-junit`
}

dependencies {

    api("org.slf4j:slf4j-api" version {slf4j})

    implementation("org.apache.tomcat:servlet-api" version { tomcatServletApi })
    implementation("io.ktor:ktor-server-core" version {ktor})
    implementation("org.junit.jupiter:junit-jupiter-api" version {jUnit})
}