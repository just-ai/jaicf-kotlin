package com.justai.jaicf.channel.viber.sdk.message

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#contact-message
 */
data class Contact @JvmOverloads constructor(
    val name: String,
    val phoneNumber: String,
    val avatar: String? = null
)
