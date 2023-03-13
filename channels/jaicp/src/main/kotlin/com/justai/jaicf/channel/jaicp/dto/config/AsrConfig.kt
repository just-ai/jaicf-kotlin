package com.justai.jaicf.channel.jaicp.dto.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Configuration parameters for the automatic speech recognition (ASR) provider used in a telephone channel.
 *
 * The properties for each ASR provider are different and are defined in the relevant classes.
 * Only one provider can be specified in the configuration.
 *
 * @param type is asr provider of the current session.
 * @param yandex configuration options for the Yandex ASR provider, if used.
 * @param zitech configuration options for the Zitech ASR provider, if used.
 * @param google configuration options for the Google ASR provider, if used.
 * @param aimyvoice configuration options for the Aimyvoice ASR provider, if used.
 * @param mts configuration options for the MTS ASR provider, if used.
 * @param azure configuration options for the Azure ASR provider, if used.
 * @param asm configuration options for the ASM ASR provider, if used.
 * @param sber configuration options for the Sber ASR provider, if used.
 **/
@Serializable
data class AsrConfig(
    val type: AsrProviderType? = null,
    val yandex: AsrYandexConfig? = null,
    val zitech: AsrZitechConfig? = null,
    val google: AsrGoogleConfig? = null,
    @SerialName("amiVoice")
    val aimyvoice: AsrAimyvoiceConfig? = null,
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
        AIMYVOICE,

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

/**
 * Base class for ASR provider configuration.
 * Subclasses contain provider-specific settings that are used to configure the ASR provider for the current session.
 **/
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
data class AsrAimyvoiceConfig(
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