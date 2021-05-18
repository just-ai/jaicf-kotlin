package com.justai.jaicf.channel.viber.sdk.message

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#rich-media-message--carousel-content-message
 */
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
data class RichMediaObject @JvmOverloads constructor(
    val buttons: List<Button>,
    val buttonsGroupColumns: Int? = null,
    val buttonsGroupRows: Int? = null,
    @JsonProperty("BgColor")
    val backgroundColor: String? = null
) {
    val type: String = "rich_media"

    companion object {
        const val DEFAULT_COLUMNS_COUNT = 6
        const val DEFAULT_ROWS_COUNT = 7
    }
}
