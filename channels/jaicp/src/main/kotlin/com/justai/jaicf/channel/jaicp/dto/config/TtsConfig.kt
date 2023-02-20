package com.justai.jaicf.channel.jaicp.dto.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Properties for TTS provider setting for telephone channel.
 *
 * The properties for each provider are different and determined in the relevant classes.
 * Not null there can be only one provider.
 * @param type is provider of the current session.
 *
 **/
@Serializable
data class TtsConfig(
    val type: TtsProviderType? = null,
    val yandex: TtsConfigYandex? = null,
    val google: TtsConfigGoogle? = null,
    val mts: TtsConfigMts? = null,
    val zitech: TtsConfigZitech? = null,
    val azure: TtsConfigAzure? = null,
    val aimyvoice: TtsConfigAimyvoice? = null,
    val sber: TtsConfigSber? = null,
) {
    @Serializable
    enum class TtsProviderType {
        @SerialName("yandex")
        YANDEX,

        @SerialName("google")
        GOOGLE,

        @SerialName("mts")
        MTS,

        @SerialName("zitech")
        ZITECH,

        @SerialName("azure")
        AZURE,

        @SerialName("aimyvoice")
        AIMYVOICE,

        @SerialName("sber")
        SBER
    }
}

@Serializable
sealed class TtsProviderConfig

@Serializable
data class TtsConfigAimyvoice(
    val voice: String? = null
) : TtsProviderConfig()

@Serializable
data class TtsConfigYandex(
    val lang: String? = null,
    val voice: String? = null,
    val emotion: String? = null,
    val speed: Double? = null,
    val volume: Double? = null,
    val useV3: Boolean? = null,
    val useVariables: Boolean? = null,
) : TtsProviderConfig()

@Serializable
data class TtsConfigMts(
    val sampleRate: Int? = null,
    val lang: String? = null,
    val mediaType: String? = null,
    val modelType: String? = null,
    val voice: String? = null
) : TtsProviderConfig()

@Serializable
data class TtsConfigGoogle(
    val voice: String? = null,
    val pitch: Double? = null,
    val speakingRate: Double? = null,
    val volumeGain: Double? = null
) : TtsProviderConfig()

@Serializable
data class TtsConfigZitech(
    val sampleRate: Int? = null,
    val model: String? = null,
    val speed: Double? = null,
    val tone: Double? = null
) : TtsProviderConfig()

@Serializable
data class TtsConfigAzure(
    val sampleRate: Int? = null,
    val voiceName: String? = null
) : TtsProviderConfig()

@Serializable
data class TtsConfigSber(
    val voice: String? = null
) : TtsProviderConfig()