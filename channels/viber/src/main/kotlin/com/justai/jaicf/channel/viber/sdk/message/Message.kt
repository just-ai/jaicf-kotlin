package com.justai.jaicf.channel.viber.sdk.message

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = RichMediaMessage::class, name = "rich_media"),
    JsonSubTypes.Type(value = TextMessage::class, name = "text"),
    JsonSubTypes.Type(value = ContactMessage::class, name = "contact"),
    JsonSubTypes.Type(value = FileMessage::class, name = "file"),
    JsonSubTypes.Type(value = LocationMessage::class, name = "location"),
    JsonSubTypes.Type(value = PictureMessage::class, name = "picture"),
    JsonSubTypes.Type(value = StickerMessage::class, name = "sticker"),
    JsonSubTypes.Type(value = UrlMessage::class, name = "url"),
    JsonSubTypes.Type(value = VideoMessage::class, name = "video"),
)
sealed class Message {
    abstract val type: String?
    abstract val keyboard: Keyboard?
    abstract val trackingData: String?
    abstract val minApiVersion: Int?
}

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#contact-message
 */
data class ContactMessage @JvmOverloads constructor(
    val contact: Contact,
    override val keyboard: Keyboard? = null,
    override val trackingData: String? = null,
    override val minApiVersion: Int? = null
) : Message() {
    override val type = "contact"
}

/**
 * @param filename should include allowable extension.
 * @link https://developers.viber.com/docs/api/rest-bot-api/#file-message
 * @link https://developers.viber.com/docs/api/rest-bot-api/#forbiddenFileFormats
 */
data class FileMessage @JvmOverloads constructor(
    @JsonProperty("media")
    val url: String,
    @JsonProperty("size")
    val size: Int,
    @JsonProperty("file_name")
    val filename: String,
    override val keyboard: Keyboard? = null,
    override val trackingData: String? = null,
    override val minApiVersion: Int? = null
) : Message() {
    override val type = "file"
}

/**
 * The message for sending only the keyboard without any other payload
 */
data class KeyboardMessage @JvmOverloads constructor(
    override val keyboard: Keyboard,
    override val trackingData: String? = null,
    override val minApiVersion: Int? = null
) : Message() {
    @Transient
    override val type: String? = null
}

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#location-message
 */
data class LocationMessage @JvmOverloads constructor(
    val location: Location,
    override val keyboard: Keyboard? = null,
    override val trackingData: String? = null,
    override val minApiVersion: Int? = null
) : Message() {
    override val type = "location"
}

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#picture-message
 */
data class PictureMessage @JvmOverloads constructor(
    @JsonProperty("media")
    val url: String,
    @JsonProperty("text")
    val description: String? = null,
    val thumbnail: String? = null,
    @JsonProperty("file_name")
    val filename: String? = null,
    override val keyboard: Keyboard? = null,
    override val trackingData: String? = null,
    override val minApiVersion: Int? = null
) : Message() {
    override val type = "picture"
}

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#rich-media-message--carousel-content-message
 */
data class RichMediaMessage @JvmOverloads constructor(
    @JsonProperty("rich_media")
    val richMediaObject: RichMediaObject,
    @JsonProperty("alt_name")
    val alternativeText: String? = null,
    override val keyboard: Keyboard? = null,
    override val trackingData: String? = null,
    override val minApiVersion: Int? = 7
) : Message() {
    override val type = "rich_media"
}

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#sticker-message
 */
data class StickerMessage @JvmOverloads constructor(
    val stickerId: Int,
    @JsonProperty("media")
    val url: String? = null,
    override val keyboard: Keyboard? = null,
    override val trackingData: String? = null,
    override val minApiVersion: Int? = null
) : Message() {
    override val type = "sticker"
}

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#text-message
 */
data class TextMessage @JvmOverloads constructor(
    val text: String,
    override val keyboard: Keyboard? = null,
    override val trackingData: String? = null,
    override val minApiVersion: Int? = null
) : Message() {
    override val type = "text"
}

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#url-message
 */
data class UrlMessage @JvmOverloads constructor(
    @JsonProperty("media")
    val url: String,
    override val keyboard: Keyboard? = null,
    override val trackingData: String? = null,
    override val minApiVersion: Int? = null
) : Message() {
    override val type = "url"
}

/**
 * @link https://developers.viber.com/docs/api/rest-bot-api/#video-message
 */
data class VideoMessage @JvmOverloads constructor(
    @JsonProperty("media")
    val url: String,
    val size: Int,
    val text: String? = null,
    val thumbnail: String? = null,
    val duration: Int? = null,
    @JsonProperty("file_name")
    val filename: String? = null,
    override val keyboard: Keyboard? = null,
    override val trackingData: String? = null,
    override val minApiVersion: Int? = null
) : Message() {
    override val type = "video"
}
