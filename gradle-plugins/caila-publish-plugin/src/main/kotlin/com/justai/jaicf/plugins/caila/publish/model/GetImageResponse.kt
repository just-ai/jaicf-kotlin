package com.justai.jaicf.plugins.caila.publish.model

import kotlinx.serialization.Serializable

@Serializable
data class GetImageResponse(
    val paging: Paging,
    val records: List<ImageRecord>,
)

@Serializable
data class Paging(
    val totalElements: Int,
    val totalPages: Int,
    val pageNumber: Int,
    val pageSize: Int,
)

@Serializable
data class ImageRecord(
    val id: ImageId,
    val name: String,
    val imageAccountName: String,
    val image: String,
    val accessMode: String,
)