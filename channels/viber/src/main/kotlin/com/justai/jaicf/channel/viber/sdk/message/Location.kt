package com.justai.jaicf.channel.viber.sdk.message

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#location-message
 */
data class Location(
    @JsonProperty("lat")
    val latitude: Double,
    @JsonProperty("lon")
    val longitude: Double
)
