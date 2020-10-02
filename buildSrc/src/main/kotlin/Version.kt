object Version {
    // Kotlin
    const val kotlin = "1.3.61"
    const val stdLib = "1.3.61"

    // Libraries
    const val jackson = "2.10.0"
    const val slf4j = "1.7.30"
    const val jUnit = "5.6.0"
    const val jetty = "9.4.3.v20170317"

    const val ktor = "1.3.1"
    const val serializationRuntime = "0.14.0"
    const val coroutinesCore = "1.3.3"
    const val javaxServletApi = "3.0.1"
}

infix fun String.version(versionProvider: Version.() -> String) =
    "$this:${versionProvider(Version)}"