package com.justai.jaicf.activator.caila.dto

import kotlinx.serialization.Serializable

@Serializable
data class EntitiesLookupResults(
    val text: String? = null,
    val entities: List<EntityMarkupData>? = null
)

@Serializable
data class EntityMarkupData(
    val entity: String,
    val slot: String? = null,
    val startPos: Int,
    val endPos: Int,
    val text: String,
    val value: String?,
    val default: Boolean?,
    val system: Boolean?,
    val entityId: Long?
)