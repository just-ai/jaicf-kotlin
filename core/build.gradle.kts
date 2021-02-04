plugins {
    `jaicf-kotlin`
    `jaicf-publish`
    `jaicf-junit`
}

dependencies {

    api(slf4j("slf4j-api"))

    implementation(`tomcat-servlet`())
    implementation(ktor("ktor-server-core"))
    implementation("org.junit.jupiter:junit-jupiter-api" version {jUnit})
}