package com.justai.jaicf.helpers.ssml

import com.justai.jaicf.helpers.http.toUrl

val break200ms = breakMs(200)
val break300ms = breakMs(300)
val break500ms = breakMs(500)
val break1s = breakS(1)

fun breakMs(ms: Int) = "<break time='${ms}ms'/>"
fun breakS(s: Int) = "<break time='${s}s'/>"

fun ordinal(number: Int) = "<say-as interpret-as='ordinal'>$number</say-as>"

fun prosody(text:String, rate: String = "medium", pitch: String = "medium", volume: String = "medium")
        = "<prosody pitch='$pitch' rate='$rate' volume='$volume'>$text</prosody>"

fun xslow(text: String) = prosody(text, rate = "x-slow")
fun slow(text: String) = prosody(text, rate = "slow")
fun fast(text: String) = prosody(text, rate = "fast")
fun xfast(text: String) = prosody(text, rate = "x-fast")

fun audio(url: String) = "<audio src='${url.toUrl()}'/>"

//TODO pitch and volume