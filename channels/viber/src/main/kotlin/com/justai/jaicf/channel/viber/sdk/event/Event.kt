package com.justai.jaicf.channel.viber.sdk.event

import com.fasterxml.jackson.annotation.JsonProperty

enum class Event(val serverEventName: String) {
    @JsonProperty("message")
    MESSAGE_RECEIVED("message"),

    @JsonProperty("delivered")
    MESSAGE_DELIVERED("delivered"),

    @JsonProperty("seen")
    MESSAGE_SEEN("seen"),

    @JsonProperty("subscribed")
    SUBSCRIBED("subscribed"),

    @JsonProperty("unsubscribed")
    UNSUBSCRIBED("unsubscribed"),

    @JsonProperty("conversation_started")
    CONVERSATION_STARTED("conversation_started"),

    @JsonProperty("webhook")
    WEBHOOK("webhook"),

    @JsonProperty("failed")
    FAILED("failed");
}
