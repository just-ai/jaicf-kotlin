package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.dto.*
import com.justai.jaicf.channel.jaicp.dto.AudioReply
import com.justai.jaicf.channel.jaicp.dto.HangupReply
import com.justai.jaicf.channel.jaicp.dto.SwitchReply
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.reactions.Reactions
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import java.time.Instant

val Reactions.telephony
    get() = this as? TelephonyReactions

class TelephonyReactions(request: JaicpBotRequest) : JaicpReactions() {
    private val callerId = request.telephony?.caller

    /**
     * метод шоб взять и положить урлу с аудио
     * */
    override fun audio(url: String): AudioReaction {
        replies.add(AudioReply(url.toUrl()))
        return AudioReaction.create(url)
    }

    /**
     * метод шоб взять и положить трубу
     * */
    fun hangup() {
        replies.add(HangupReply())
    }

    /**
     * метод шоб взять и перезвонить
     * */
    fun redial(redialData: JaicpDialerAPI.RedialData) = dialer.redial(redialData)

    /**
     * оверлоад метода шоб взять и перезвонить
     * */
    fun redial(fromTime: Instant?, toTime: Instant?, maxAttempts: Int?) = dialer.redial(
        JaicpDialerAPI.RedialData(
            startDateTime = fromTime?.toEpochMilli(),
            finishDateTime = toTime?.toEpochMilli(),
            maxAttempts = maxAttempts
        )
    )

    /**
     * шоб выставить результат обзвона. один на клиента
     * */
    fun setResult(callResult: String?, callResultPayload: JsonObject?) =
        dialer.result(JaicpDialerAPI.CallResultData(callResult, callResultPayload))


    /**
     * шоб поставить репорт в цсвху
     * */
    fun report(header: String, value: String?, order: Int?) =
        dialer.report(JaicpDialerAPI.CallReportData(header, value, order))

    /**
     * шоб выставить тэг в цсвху. Тэг - имя колонки, пэйлоад и колор где-то в значениях или в бублике
     * */
    fun addTag(tag: String, tagPayload: JsonObject?, tagColor: String?) =
        dialer.tag(JaicpDialerAPI.CallTagData(tag, tagPayload, tagColor))

    /**
     * перевод на оператора или другого челика
     * */
    fun transferCall(phoneNumber: String, sipHeaders: Map<String, String> = emptyMap()) {
        replies.add(SwitchReply(phoneNumber, sipHeaders))
    }
}