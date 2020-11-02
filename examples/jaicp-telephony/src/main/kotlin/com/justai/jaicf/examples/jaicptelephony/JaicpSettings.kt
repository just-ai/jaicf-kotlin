package com.justai.jaicf.examples.jaicptelephony

import com.justai.jaicf.activator.caila.CailaNLUSettings
import java.util.Properties

val accessToken: String = System.getenv("JAICP_API_TOKEN") ?: Properties().run {
    load(CailaNLUSettings::class.java.getResourceAsStream("/jaicp.properties"))
    getProperty("apiToken")
}

val nluSettings = CailaNLUSettings(
    accessToken = accessToken,
    confidenceThreshold = 0.2
)