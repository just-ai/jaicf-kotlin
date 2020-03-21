package com.justai.jaicf.channel.yandexalice.api.storage

import kotlinx.serialization.Serializable

@Serializable
data class Image(
    val id: String,
    val origUrl: String,
    val size: Long
)

@Serializable
data class UploadedImage(
    val image: Image
)

@Serializable
data class Images(
    val images: List<Image>,
    val total: Int
)