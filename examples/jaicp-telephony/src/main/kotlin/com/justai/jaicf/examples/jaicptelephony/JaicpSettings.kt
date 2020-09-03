package com.justai.jaicf.examples.jaicptelephony

import com.justai.jaicf.activator.caila.CailaNLUSettings

const val accessToken = "dev-262-TnQ"
val nluSettings = CailaNLUSettings(
    accessToken = accessToken,
    confidenceThreshold = 0.2
)