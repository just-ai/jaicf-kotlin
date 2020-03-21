package com.justai.jaicf.channel.yandexalice

import com.justai.jaicf.channel.yandexalice.api.model.Card
import com.justai.jaicf.channel.yandexalice.api.model.Image
import com.justai.jaicf.channel.yandexalice.api.model.ItemsList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule

internal val JSON = Json(
    configuration = JsonConfiguration.Stable.copy(
        strictMode = false,
        classDiscriminator = "type"),

    context = SerializersModule {
        polymorphic(Card::class) {
            Image::class with Image.serializer()
            ItemsList::class with ItemsList.serializer()
        }
    }
)