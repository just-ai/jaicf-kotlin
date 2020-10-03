package com.justai.jaicf.examples.jaicptelephony

import com.justai.jaicf.activator.caila.CailaNLUSettings

const val accessToken = "<some-bot-token>"
val nluSettings = CailaNLUSettings(
    accessToken = accessToken,
    confidenceThreshold = 0.2
)