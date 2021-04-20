package com.justai.jaicf.channel.viber.sdk.message

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * @link https://developers.viber.com/docs/tools/keyboards/
 */
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
data class Button(
    val text: String?,
    val actionBody: String?,
    val actionType: String?,
    val image: String? = null,
    val silent: Boolean = false,
    @JsonProperty("BgColor")
    val backgroundColor: String? = null,
    val textSize: String? = null,
    @JsonProperty("TextVAlign")
    val textVerticalAlign: String? = null,
    @JsonProperty("TextHAlign")
    val textHorizontalAlign: String? = null,
    val map: Location? = null,
    val columns: Int? = null,
    val rows: Int? = null
)
