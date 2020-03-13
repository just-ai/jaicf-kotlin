package com.justai.jaicf.channel.aimybox

import com.justai.jaicf.channel.aimybox.api.AimyboxReply
import kotlinx.serialization.json.JsonElement

class ResponseBuilder(
    private val query: String
) {

    private var question = false
    private var action: String? = null
    private var intent: String? = null

    private val data = mutableMapOf<String, JsonElement>()
    private val replies = mutableListOf<AimyboxReply>()


}