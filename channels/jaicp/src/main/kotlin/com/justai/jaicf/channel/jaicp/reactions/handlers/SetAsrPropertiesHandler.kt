package com.justai.jaicf.channel.jaicp.reactions.handlers

import com.justai.jaicf.channel.jaicp.dto.config.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class SetAsrPropertiesHandler {

    private val setAsrPropertiesHandlerSber = SetAsrPropertiesHandlerSber()
    private val setAsrPropertiesHandlerYandex = SetAsrPropertiesHandlerYandex()
    private val setAsrPropertiesHandlerGoogle = SetAsrPropertiesHandlerGoogle()
    private val setAsrPropertiesHandlerMts = SetAsrPropertiesHandlerMts()
    private val setAsrPropertiesHandlerZitech = SetAsrPropertiesHandlerZitech()
    private val setAsrPropertiesHandlerAimyvoice = SetAsrPropertiesHandlerAimyvoice()
    private val setAsrPropertiesHandlerAzure = SetAsrPropertiesHandlerAzure()
    private val setAsrPropertiesHandlerAsm = SetAsrPropertiesHandlerAsm()
    private val setAsrPropertiesHandlerKaldi = SetAsrPropertiesHandlerKaldi()
    private val setAsrPropertiesHandlerTinkoff = SetAsrPropertiesHandlerTinkoff()

    fun handle(properties: Map<String, String>, asrConfig: AsrConfig): AsrConfig {
        val propertiesJson = JsonObject(properties.toMutableMap().mapValues { entry -> JsonPrimitive(entry.value) })
        return when (checkNotNull(asrConfig.type)) {
            AsrConfig.AsrProviderType.SBER -> {
                setAsrPropertiesHandlerSber.handle(asrConfig, propertiesJson)
            }

            AsrConfig.AsrProviderType.YANDEX -> {
                setAsrPropertiesHandlerYandex.handle(asrConfig, propertiesJson)
            }

            AsrConfig.AsrProviderType.GOOGLE -> {
                setAsrPropertiesHandlerGoogle.handle(asrConfig, propertiesJson)
            }

            AsrConfig.AsrProviderType.MTS -> {
                setAsrPropertiesHandlerMts.handle(asrConfig, propertiesJson)
            }

            AsrConfig.AsrProviderType.ZITECH -> {
                setAsrPropertiesHandlerZitech.handle(asrConfig, propertiesJson)
            }

            AsrConfig.AsrProviderType.AIMYVOICE -> {
                setAsrPropertiesHandlerAimyvoice.handle(asrConfig, propertiesJson)
            }

            AsrConfig.AsrProviderType.AZURE -> {
                setAsrPropertiesHandlerAzure.handle(asrConfig, propertiesJson)
            }

            AsrConfig.AsrProviderType.ASM -> {
                setAsrPropertiesHandlerAsm.handle(asrConfig, propertiesJson)
            }

            AsrConfig.AsrProviderType.TINKOFF -> {
                setAsrPropertiesHandlerTinkoff.handle(asrConfig, propertiesJson)
            }

            AsrConfig.AsrProviderType.KALDI -> {
                setAsrPropertiesHandlerKaldi.handle(asrConfig, propertiesJson)
            }
        }
    }
}

abstract class SetAsrPropertiesHandlerAbstract {
    abstract fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig
}

class SetAsrPropertiesHandlerSber() : SetAsrPropertiesHandlerAbstract() {
    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrSberConfig = checkNotNull(asrConfig.sber)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            sber = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerYandex() : SetAsrPropertiesHandlerAbstract() {
    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrYandexConfig = checkNotNull(asrConfig.yandex)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            yandex = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerGoogle() : SetAsrPropertiesHandlerAbstract() {
    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrGoogleConfig = checkNotNull(asrConfig.google)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            google = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerMts() : SetAsrPropertiesHandlerAbstract() {
    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrMtsConfig = checkNotNull(asrConfig.mts)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            mts = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerZitech() : SetAsrPropertiesHandlerAbstract() {
    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrZitechConfig = checkNotNull(asrConfig.zitech)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            zitech = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerAimyvoice() : SetAsrPropertiesHandlerAbstract() {
    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrAimyvoiceConfig = checkNotNull(asrConfig.aimyvoice)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            aimyvoice = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerAzure() : SetAsrPropertiesHandlerAbstract() {
    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrAzureConfig = checkNotNull(asrConfig.azure)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            azure = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerAsm() : SetAsrPropertiesHandlerAbstract() {
    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrAsmConfig = checkNotNull(asrConfig.asm)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            asm = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerKaldi() : SetAsrPropertiesHandlerAbstract() {
    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        return asrConfig.copy(
            asrProperties = propertiesJson
        )
    }
}

class SetAsrPropertiesHandlerTinkoff() : SetAsrPropertiesHandlerAbstract() {
    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        return asrConfig.copy(
            asrProperties = propertiesJson
        )
    }
}