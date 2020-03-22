package com.justai.jaicf.helpers.ssml

import com.justai.jaicf.helpers.http.toUrl

/**
 * SSML (Speech Synthesis Markup Language) helper functions allow to add SSML tags to the string output.
 * SSML is supported by some voice-enabled channels like Alexa and Google Actions.
 *
 * Usage example:
 *
 * ```
 *  action {
 *    reactions.say("Hello $break200ms there! You're the ${ordinal(100)} user.")
 *    reactions.say("Just listen this. ${audio("https://someaddress.com/audio.mp3")}")
 *  }
 * ```
 */

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