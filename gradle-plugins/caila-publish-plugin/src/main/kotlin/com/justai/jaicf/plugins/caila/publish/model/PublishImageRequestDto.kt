package com.justai.jaicf.plugins.caila.publish.model

import kotlinx.serialization.Serializable

@Serializable
data class PublishImageRequestDto(
    val name: String,
    val image: String,
    val accessMode: String? = null,
)

@Serializable
data class PublishImageResponseDto(
    val name: String,
    val image: String,
    val accessMode: String? = null,
    val id: ImageId,
)
