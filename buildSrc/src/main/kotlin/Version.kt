object Version {
    // Kotlin
    const val kotlin = "1.4.10"
    const val stdLib = "1.4.10"

    // Libraries
    const val jackson = "2.10.0"
    const val slf4j = "1.7.30"
    const val jUnit = "5.6.0"
    const val jetty = "9.4.3.v20170317"

    const val ktor = "1.4.0"
    const val serializationRuntime = "1.0.0"
    const val coroutinesCore = "1.3.9"
    const val tomcatServletApi = "6.0.53"
    const val logbackGelfAppender = "1.5"
    const val mockk = "1.10.2"
}

infix fun String.version(versionProvider: Version.() -> String) =
    "$this:${versionProvider(Version)}"