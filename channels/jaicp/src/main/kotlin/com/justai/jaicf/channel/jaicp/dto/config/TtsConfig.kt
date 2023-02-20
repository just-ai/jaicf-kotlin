package com.justai.jaicf.channel.jaicp.dto.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Configuration parameters for the text-to-speech (TTS) provider used in a telephone channel.
 *
 * The properties for each TTS provider are different and are defined in the relevant classes.
 * Only one provider can be specified in the configuration.
 *
 * @param type is provider TTS of the current session.
 * @param yandex configuration options for the Yandex TTS provider, if used.
 * @param google configuration options for the Google TTS provider, if used.
 * @param mts configuration options for the MTS TTS provider, if used.
 * @param zitech configuration options for the Zitech TTS provider, if used.
 * @param azure configuration options for the Azure TTS provider, if used.
 * @param aimyvoice configuration options for the Aimyvoice TTS provider, if used.
 * @param sber configuration options for the Sber TTS provider, if used.
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

/**
 * Base class for TTS provider configuration.
 * Subclasses contain provider-specific settings that are used to configure the TTS provider for the current session.
 **/
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