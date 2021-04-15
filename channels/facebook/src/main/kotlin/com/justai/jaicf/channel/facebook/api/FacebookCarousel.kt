package com.justai.jaicf.channel.facebook.api

import com.github.messenger4j.common.WebviewHeightRatio
import com.github.messenger4j.common.WebviewShareButtonState
import com.github.messenger4j.messengerprofile.DeleteMessengerSettingsPayload
import com.github.messenger4j.send.message.template.common.DefaultAction
import com.github.messenger4j.send.message.template.common.Element
import java.net.URL
import java.util.*
import com.github.messenger4j.send.message.template.button.Button

data class CarouselElement(
    val title: String,
    val subtitle: String? = null,
    val imageUrl: URL? = null,
    val defaultAction: Action? = null,
    val buttons: List<Button>? = null
)

data class Action(
    val url: URL,
    val webviewHeightRatio: WebviewHeightRatio? = null,
    val messengerExtensions: Boolean? = null,
    val fallbackUrl: URL? = null,
    val webviewShareButtonState: WebviewShareButtonState? = null
)

internal fun CarouselElement.toTemplateElement(): Element = Element.create(
    title,
    Optional.ofNullable(subtitle),
    Optional.ofNullable(imageUrl),
    Optional.ofNullable(defaultAction?.toTemplateAction()),
    Optional.ofNullable(buttons)
)

internal fun Action.toTemplateAction() = DefaultAction.create(
    url,
    Optional.ofNullable(webviewHeightRatio),
    Optional.ofNullable(messengerExtensions),
    Optional.ofNullable(fallbackUrl),
    Optional.ofNullable(webviewShareButtonState)
)
