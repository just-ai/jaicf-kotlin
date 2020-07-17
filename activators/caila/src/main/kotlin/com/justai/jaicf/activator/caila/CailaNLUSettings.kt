package com.justai.jaicf.activator.caila

const val DEFAULT_CAILA_URL = "https://app.jaicp.com/cailapub"

data class CailaNLUSettings(
    val accessToken: String,
    val confidenceThreshold: Double = 0.2,
    val cailaUrl: String = "$DEFAULT_CAILA_URL/api/caila/p"
)