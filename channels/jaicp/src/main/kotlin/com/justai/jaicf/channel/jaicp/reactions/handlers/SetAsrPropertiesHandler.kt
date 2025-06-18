package com.justai.jaicf.channel.jaicp.reactions.handlers

import com.justai.jaicf.channel.jaicp.dto.config.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class SetAsrPropertiesHandler(
    private val listOfActualHandlers: List<SetAsrPropertiesHandlerAbstract>
) {

    fun handle(properties: Map<String, Any>, asrConfig: AsrConfig): AsrConfig {
        val propertiesJson = JsonObject(properties.mapValues { entry ->
            when (val value = entry.value) {
                is Collection<*> -> JsonArray(value.map { JsonPrimitive(it.toString()) })
                is String -> JsonPrimitive(value)
                is Map<*,*> -> JsonObject(value.mapKeys { it.key.toString() }.mapValues { JsonPrimitive(it.value.toString()) })
                else -> throw IllegalArgumentException("Unsupported property type: ${value::class.simpleName}")
            }
        })
        return listOfActualHandlers.first { it.canHandle(checkNotNull(asrConfig.type)) }
            .handle(asrConfig, propertiesJson)
    }
}

interface SetAsrPropertiesHandlerAbstract {
    fun canHandle(type: AsrConfig.AsrProviderType): Boolean
    fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig
}

class SetAsrPropertiesHandlerSber() : SetAsrPropertiesHandlerAbstract {
    override fun canHandle(type: AsrConfig.AsrProviderType): Boolean =
        type == AsrConfig.AsrProviderType.SBER

    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrSberConfig = checkNotNull(asrConfig.sber)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            sber = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerYandex() : SetAsrPropertiesHandlerAbstract {
    override fun canHandle(type: AsrConfig.AsrProviderType): Boolean =
        type == AsrConfig.AsrProviderType.YANDEX

    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrYandexConfig = checkNotNull(asrConfig.yandex)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            yandex = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerGoogle() : SetAsrPropertiesHandlerAbstract {
    override fun canHandle(type: AsrConfig.AsrProviderType): Boolean =
        type == AsrConfig.AsrProviderType.GOOGLE

    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrGoogleConfig = checkNotNull(asrConfig.google)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            google = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerMts() : SetAsrPropertiesHandlerAbstract {
    override fun canHandle(type: AsrConfig.AsrProviderType): Boolean =
        type == AsrConfig.AsrProviderType.MTS

    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrMtsConfig = checkNotNull(asrConfig.mts)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            mts = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerZitech() : SetAsrPropertiesHandlerAbstract {
    override fun canHandle(type: AsrConfig.AsrProviderType): Boolean =
        type == AsrConfig.AsrProviderType.ZITECH

    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrZitechConfig = checkNotNull(asrConfig.zitech)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            zitech = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerAimyvoice() : SetAsrPropertiesHandlerAbstract {
    override fun canHandle(type: AsrConfig.AsrProviderType): Boolean =
        type == AsrConfig.AsrProviderType.AIMYVOICE

    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrAimyvoiceConfig = checkNotNull(asrConfig.aimyvoice)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            aimyvoice = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerAzure() : SetAsrPropertiesHandlerAbstract {
    override fun canHandle(type: AsrConfig.AsrProviderType): Boolean =
        type == AsrConfig.AsrProviderType.AZURE

    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrAzureConfig = checkNotNull(asrConfig.azure)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            azure = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerAsm() : SetAsrPropertiesHandlerAbstract {
    override fun canHandle(type: AsrConfig.AsrProviderType): Boolean =
        type == AsrConfig.AsrProviderType.ASM

    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        val asrProviderConfig: AsrAsmConfig = checkNotNull(asrConfig.asm)
        return asrConfig.copy(
            asrProperties = propertiesJson,
            asm = asrProviderConfig.copy(asrProperties = propertiesJson)
        )
    }
}

class SetAsrPropertiesHandlerKaldi() : SetAsrPropertiesHandlerAbstract {
    override fun canHandle(type: AsrConfig.AsrProviderType): Boolean =
        type == AsrConfig.AsrProviderType.KALDI

    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        return asrConfig.copy(
            asrProperties = propertiesJson
        )
    }
}

class SetAsrPropertiesHandlerTinkoff() : SetAsrPropertiesHandlerAbstract {
    override fun canHandle(type: AsrConfig.AsrProviderType): Boolean =
        type == AsrConfig.AsrProviderType.TINKOFF

    override fun handle(asrConfig: AsrConfig, propertiesJson: JsonObject): AsrConfig {
        return asrConfig.copy(
            asrProperties = propertiesJson
        )
    }
}