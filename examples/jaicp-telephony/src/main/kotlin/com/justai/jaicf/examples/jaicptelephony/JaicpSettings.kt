package com.justai.jaicf.examples.jaicptelephony

import com.justai.jaicf.activator.caila.CailaNLUSettings

const val caUrl = "http://jaicf-dev.lo.test-ai.net/chatadapter"
const val clpUrl = "http://jaicf-dev.lo.test-ai.net/cailapub"

const val accessToken = "85218c3f-e111-435b-9577-055c6e2f38e2"
val nluSettings = CailaNLUSettings(
    accessToken = accessToken,
    confidenceThreshold = 0.2,
    cailaUrl = "$clpUrl/api/caila/p"
)
