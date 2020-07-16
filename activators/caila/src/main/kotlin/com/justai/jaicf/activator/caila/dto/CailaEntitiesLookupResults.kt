package com.justai.jaicf.activator.caila.dto

import kotlinx.serialization.Serializable

@Serializable
data class CailaEntitiesLookupResults(
    val text: String,
    val entities: List<CailaEntityMarkupData>
)

@Serializable
data class CailaEntityMarkupData(
    val entity: String,
    var slot: String? = null,
    val startPos: Int,
    val endPos: Int,
    val text: String,
    val value: String,
    val default: Boolean?,
    val system: Boolean?,
    val entityId: Long?
)