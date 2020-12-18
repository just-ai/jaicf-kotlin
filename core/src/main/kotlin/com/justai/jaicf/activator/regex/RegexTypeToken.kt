package com.justai.jaicf.activator.regex

import com.justai.jaicf.activator.catchall.CatchAllActivatorContext
import com.justai.jaicf.generic.ActivatorTypeToken

typealias RegexTypeToken = ActivatorTypeToken<RegexActivatorContext>

val regex: RegexTypeToken = ActivatorTypeToken()