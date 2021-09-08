package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DefaultActionContext
import kotlinx.serialization.Serializable

/**
 * Provides JAICP Analytics API for scenarios
 *
 * @see JaicpAnalyticsAPI
 * */
val DefaultActionContext.jaicpAnalytics: JaicpAnalyticsAPI
    get() = reactions.botContext.analyticsApi

internal val BotContext.analyticsApi: JaicpAnalyticsAPI
    get() = (temp[ANALYTICS_BOT_CONTEXT_KEY] as? JaicpAnalyticsAPI ?: JaicpAnalyticsAPI()).also {
        temp[ANALYTICS_BOT_CONTEXT_KEY] = it
    }

private const val ANALYTICS_BOT_CONTEXT_KEY = "com/justai/jaicf/jaicp/jaicpAnalyticsApi"

@Serializable
data class JaicpAnalyticsAPI internal constructor(
    private var sessionResult: String? = null,
    private var comment: String? = null,
    private val sessionData: MutableMap<String, String> = mutableMapOf(),
    private val sessionLabels: MutableList<String> = mutableListOf(),
    private val messageLabels: MutableList<MessageLabel> = mutableListOf(),
) {
    /**
     * Adds columns with arbitrary data in the session result report.
     *
     * @param header of column
     * @param value any stringified value
     * */
    fun setSessionData(header: String, value: String) {
        this.sessionData[header] = value
    }

    /**
     * Sets comments to client phrases (input).
     *
     * @param comment for client phrase.
     * */
    fun setComment(comment: String) {
        this.comment = comment
    }

    /**
     * Sets labels to dialogs.
     *
     * @param label displayed in Analytics section in JAICP Application Console and xls report
     * */
    fun setSessionLabel(vararg label: String) {
        this.sessionLabels.addAll(label)
    }

    /**
     * Sets labels to client phrases (input).
     *
     * @param labelName the label name in a group
     * @param groupName the name of labels group
     * */
    fun setMessageLabel(labelName: String, groupName: String) {
        setMessageLabel(MessageLabel(labelName, groupName))
    }

    /**
     * Sets labels to client phrases (input).
     *
     * @param labels
     * */
    fun setMessageLabel(vararg labels: MessageLabel) {
        messageLabels.addAll(labels)
    }

    /**
     * Sets the conversation result.
     *
     * @param result displayed in Analytics section in JAICP Application Console and xls report.
     * */
    fun setSessionResult(result: String) {
        this.sessionResult = result
    }

    @Serializable
    data class MessageLabel(
        val labelName: String,
        val groupName: String,
    )
}
