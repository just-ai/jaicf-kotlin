package com.justai.jaicf.channel.jaicp.dto.bargein

import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInMode.*
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInTrigger.FINAL
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInTrigger.INTERIM
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SimpleBargeInData(
    val bargeIn: BargeInMode,
    val bargeInTrigger: BargeInTrigger,
    val noInterruptTime: Int
)

/**
 * Barge-In is speech synthesis interruption. Following modes can be used to set interruption behaviour.
 *
 * @property DISABLED disables bargeIn for current response
 * @property FORCED interrupts synthesis immediately, without finishing current answer
 * @property PHRASE interrupts synthesis after phrase is said.
 * */
@Serializable
enum class BargeInMode {
    @SerialName("disabled")
    DISABLED,

    @SerialName("forced")
    FORCED,

    @SerialName("phrase")
    PHRASE
}

/**
 * Barge-In is speech synthesis interruption. Following modes can be used to set what triggers the interruption
 *
 * @property INTERIM triggers bargeIn with partial speech recognition results.
 * @property FINAL triggers bargeIn with final speech recognition results.
 * */
@Serializable
enum class BargeInTrigger {
    @SerialName("interim")
    INTERIM,

    @SerialName("final")
    FINAL
}