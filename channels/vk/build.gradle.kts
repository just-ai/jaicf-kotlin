import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin VK Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Vkontakte Channel implementation. Enables JAICF-Kotlin integration with vk.com"

plugins {
    `jaicf-kotlin`
    `jaicf-publish`
    `jaicf-junit`
}

val vkSdk = "1.0.10-SNAPSHOT"
val log4jAdapter = "2.14.1"

dependencies {
    core()
    api("com.vk.api:sdk:$vkSdk")
    api("org.apache.logging.log4j:log4j-to-slf4j:$log4jAdapter")
}
