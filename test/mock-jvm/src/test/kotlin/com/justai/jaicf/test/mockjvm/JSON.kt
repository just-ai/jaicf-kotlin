package com.justai.jaicf.test.mockjvm

import kotlinx.serialization.json.Json

internal val JSON = Json { ignoreUnknownKeys = true; isLenient = true }