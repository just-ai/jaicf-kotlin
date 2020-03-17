package com.justai.jaicf.activator.caila

data class CailaNLUSettings(
    val accessToken: String,
    val confidenceThreshold: Double = 0.2,
    val cailaUrl: String = "http://jaicf01-demo-htz.lab.just-ai.com/cailapub/api/caila/p"
)