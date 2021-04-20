package com.justai.jaicf.channel.viber.sdk.profile

sealed class Profile {
    abstract val name: String?
    abstract val avatar: String?
}

data class BotProfile @JvmOverloads constructor(
    override val name: String,
    override val avatar: String? = null
) : Profile()

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#response-2
 */
data class UserProfile @JvmOverloads constructor(
    val id: String,
    val country: String = "",
    val language: String = "",
    val apiVersion: Int = 7,
    override val name: String? = null,
    override val avatar: String? = null
) : Profile()
