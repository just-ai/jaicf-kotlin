package com.justai.jaicf.channel.viber.api

object ViberEvent {
    const val SEEN = "seen"
    const val DELIVERED = "delivered"
    const val SUBSCRIBED = "subscribed"
    const val UNSUBSCRIBED = "unsubscribed"
    const val CONVERSATION_STARTED = "conversation_started"
    const val FAILED = "failed"

    const val RICH_MEDIA_MESSAGE = "rich_media"
    const val CONTACT_MESSAGE = "contact"
    const val FILE_MESSAGE = "file"
    const val LOCATION_MESSAGE = "location"
    const val IMAGE_MESSAGE = "image"
    const val STICKER_MESSAGE = "sticker"
    const val URL_MESSAGE = "url"
    const val VIDEO_MESSAGE = "video"
}
