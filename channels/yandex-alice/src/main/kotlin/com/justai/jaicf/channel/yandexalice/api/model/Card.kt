package com.justai.jaicf.channel.yandexalice.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface Card

@Serializable
@SerialName("BigImage")
data class Image(
    @SerialName("image_id")
    val imageId: String,
    val title: String? = null,
    val description: String? = null,
    val button: Button? = null
) : Card

@Serializable
@SerialName("ItemsList")
data class ItemsList(
    val header: Header? = null,
    val footer: Footer? = null,
    val items: MutableList<Image> = mutableListOf()
): Card {

    fun addImage(image: Image): ItemsList = apply { items.add(image) }

    @Serializable
    data class Header(val text: String? = null)

    @Serializable
    data class Footer(val text: String, val button: Button? = null)
}

@Serializable
@SerialName("ImageGallery")
data class ImageGallery(
    private val items: List<Image>
): Card