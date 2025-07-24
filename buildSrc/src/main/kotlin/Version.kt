object Version {
    // Kotlin
    const val kotlin = "2.0.20"

    // Libraries
    const val jUnit = "5.13.1"

    const val serializationRuntime = "1.0.1"
}

infix fun String.version(versionProvider: Version.() -> String) = "$this:${versionProvider(Version)}"
