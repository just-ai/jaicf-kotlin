plugins {
    `jaicf-kotlin`
    `jaicf-publish`
    `jaicf-junit`
}

val vkSdk = "1.0.6"
val log4jAdapter = "2.14.1"

dependencies {
    core()
    api("com.vk.api:sdk:$vkSdk")
    api("org.apache.logging.log4j:log4j-to-slf4j:$log4jAdapter")

    testImplementation("ch.qos.logback:logback-classic:1.2.3")
}
