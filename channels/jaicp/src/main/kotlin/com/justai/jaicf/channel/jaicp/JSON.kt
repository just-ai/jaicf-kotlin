package com.justai.jaicf.channel.jaicp

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

internal val JSON = Json(JsonConfiguration.Stable.copy(strictMode = false, encodeDefaults = false))
