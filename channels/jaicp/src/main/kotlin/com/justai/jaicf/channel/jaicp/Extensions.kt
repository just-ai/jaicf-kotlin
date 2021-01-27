package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.channel.jaicp.dto.jaicpNative
import com.justai.jaicf.logging.ExecutionContext

internal val ExecutionContext.jaicpRequest: JaicpBotRequest?
    get() {
        return request.jaicpNative?.jaicp ?: try {
            requestContext.httpBotRequest?.requestMetadata?.let {
                JSON.decodeFromString(JaicpBotRequest.serializer(), it)
            }
        } catch (e: Exception) {
            return null
        }
    }