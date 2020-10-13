package com.justai.jaicf.channel.aimybox

import com.justai.jaicf.channel.aimybox.api.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

internal val JSON = Json {

    ignoreUnknownKeys = true
    classDiscriminator = "type"

    serializersModule = SerializersModule {
        polymorphic(AimyboxReply::class, TextReply::class, TextReply.serializer())
        polymorphic(AimyboxReply::class, ImageReply::class, ImageReply.serializer())
        polymorphic(AimyboxReply::class, AudioReply::class, AudioReply.serializer())
        polymorphic(AimyboxReply::class, ButtonsReply::class, ButtonsReply.serializer())

        polymorphic(Button::class, TextButton::class, TextButton.serializer())
        polymorphic(Button::class, UrlButton::class, UrlButton.serializer())
        polymorphic(Button::class, PayloadButton::class, PayloadButton.serializer())
    }
}