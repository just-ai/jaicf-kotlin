package com.justai.jaicf.examples.citiesgame

import com.justai.jaicf.activator.caila.CailaNLUSettings

val projectId = "mva_test_jaicf-263-YBW"
val nluSettings = CailaNLUSettings(
    projectId = projectId,
    confidenceThreshold = 0.2
)