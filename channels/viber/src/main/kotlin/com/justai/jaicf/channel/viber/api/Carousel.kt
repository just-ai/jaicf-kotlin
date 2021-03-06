package com.justai.jaicf.channel.viber.api

import com.justai.jaicf.channel.viber.sdk.api.NoActionButton
import com.justai.jaicf.channel.viber.sdk.api.Size
import com.justai.jaicf.channel.viber.sdk.api.ViberButton
import com.justai.jaicf.channel.viber.sdk.api.toButton
import com.justai.jaicf.channel.viber.sdk.message.Button
import com.justai.jaicf.channel.viber.sdk.message.RichMediaObject

data class Carousel(
    val elements: List<CarouselElement>,
    val backgroundColor: String = "#FFFFFF"
)

data class CarouselElement(
    val imageUrl: String,
    val title: String,
    val subtitle: String? = null,
    val button: ViberButton? = null
)

internal fun Carousel.toRichMediaObject() = RichMediaObject(
    elements.flatMap(CarouselElement::toButtons),
    RichMediaObject.DEFAULT_COLUMNS_COUNT,
    RichMediaObject.DEFAULT_ROWS_COUNT,
    backgroundColor
)

internal fun CarouselElement.toButtons(): List<Button> = listOf(
    NoActionButton(text = "", imageUrl = imageUrl, columns = RichMediaObject.DEFAULT_COLUMNS_COUNT, rows = 3).toButton(),
    NoActionButton(
        text = "<b>$title</b>",
        ViberButton.Style(textSize = Size.LARGE),
        columns = RichMediaObject.DEFAULT_COLUMNS_COUNT,
        rows = 2
    ).toButton(),
    NoActionButton(text = subtitle ?: "", columns = RichMediaObject.DEFAULT_COLUMNS_COUNT, rows = 1).toButton(),
    button?.toButton()?.copy(columns = RichMediaObject.DEFAULT_COLUMNS_COUNT, rows = 1)
        ?: NoActionButton(
            text = "",
            columns = RichMediaObject.DEFAULT_COLUMNS_COUNT,
            rows = 1
        ).toButton()
)
