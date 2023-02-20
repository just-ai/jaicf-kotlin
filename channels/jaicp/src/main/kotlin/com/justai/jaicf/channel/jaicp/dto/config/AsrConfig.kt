package com.justai.jaicf.channel.jaicp.dto.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Properties for ASR provider setting for telephone channel.
 *
 * The properties for each provider are different and determined in the relevant classes.
 * Not null there can be only one provider.
 * @param type is provider of the current session.
 *
 **/
@Serializable
data class AsrConfig(
    val type: AsrProviderType? = null,
    val yandex: AsrYandexConfig? = null,
    val zitech: AsrZitechConfig? = null,
    val google: AsrGoogleConfig? = null,
    val amiVoice: AsrAmiVoiceConfig? = null,
    val mts: AsrMtsConfig? = null,
    val azure: AsrAzureConfig? = null,
    val asm: AsrAsmConfig? = null,
    val sber: AsrSberConfig? = null
) {
    @Serializable
    enum class AsrProviderType {
        @SerialName("yandex")
        YANDEX,

        @SerialName("google")
        GOOGLE,

        @SerialName("tinkoff")
        TINKOFF,

        @SerialName("mts")
        MTS,

        @SerialName("zitech")
        ZITECH,

        @SerialName("amiVoice")
        AMIVOICE,

        @SerialName("azure")
        AZURE,

        @SerialName("asm")
        ASM,

        @SerialName("sber")
        SBER,

        @SerialName("kaldi")
        KALDI
    }
}

@Serializable
sealed class AsrProviderConfig

@Serializable
data class AsrGoogleConfig(
    val model: String? = null,
    val lang: String? = null
) : AsrProviderConfig()

@Serializable
data class AsrZitechConfig(
    val model: String? = null,
    val lang: String? = null
) : AsrProviderConfig()

@Serializable
data class AsrYandexConfig(
    val model: String? = null,
    val lang: String? = null,
    val numbersAsWords: Boolean? = null,
    val sensitivityReduction: Boolean? = null
) : AsrProviderConfig()

@Serializable
data class AsrMtsConfig(
    val model: String? = null,
    val lang: String? = null,
    val transferType: String? = null
) : AsrProviderConfig()

@Serializable
data class AsrAmiVoiceConfig(
    val codec: String? = null,
    val mode: String? = null,
    val grammarFileNames: String? = null
) : AsrProviderConfig()

@Serializable
data class AsrAzureConfig(
    val language: String? = null,
    val outputFormat: String? = null,
    val profanityOption: String? = null,
    val enableDictation: Boolean? = null
) : AsrProviderConfig()

@Serializable
data class AsrAsmConfig(
    val sampleRate: Long? = null,
    val model: String? = null,
) : AsrProviderConfig()

@Serializable
data class AsrSberConfig(
    val language: String? = null,
    val model: String? = null
) : AsrProviderConfig()