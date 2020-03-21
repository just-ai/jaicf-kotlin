package com.justai.jaicf.channel.aimybox

import com.justai.jaicf.channel.aimybox.api.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule

internal val JSON = Json(
    configuration = JsonConfiguration.Stable.copy(
        strictMode = false,
        classDiscriminator = "type"),

    context = SerializersModule {
        polymorphic(AimyboxReply::class) {
            TextReply::class with TextReply.serializer()
            ImageReply::class with ImageReply.serializer()
            AudioReply::class with AudioReply.serializer()
            ButtonsReply::class with ButtonsReply.serializer()
        }
        polymorphic(Button::class) {
            TextButton::class with TextButton.serializer()
            UrlButton::class with UrlButton.serializer()
            PayloadButton::class with PayloadButton.serializer()
        }
    }
)