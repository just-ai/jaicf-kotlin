package com.justai.jaicf.channel.viber.sdk.message

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * @link https://developers.viber.com/docs/tools/keyboards/
 */
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
data class Keyboard(
    val buttons: List<Button>,
    val defaultHeight: Boolean = false
) {
    val type = "keyboard"

    companion object {
        const val DEFAULT_COLUMNS_COUNT = 6
        const val DEFAULT_ROWS_COUNT = 2
    }
}
