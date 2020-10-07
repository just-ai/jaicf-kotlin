package com.justai.jaicf.channel.jaicp

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import org.slf4j.MDC

object JaicpMDC {
    fun setFromRequest(request: JaicpBotRequest) {
        MDC.put("requestId", request.questionId)
        MDC.put("channelId", request.channelBotId)
    }

    fun setAccountId(accountId: String) {
        MDC.put("accountId", accountId)
    }
}

