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
class JaicpAnalyticsAPI internal constructor() {
    private var sessionResult: String? = null
    private var comment: String? = null
    private val sessionData: MutableMap<String, String> = mutableMapOf();
    private val sessionLabel: MutableList<String> = mutableListOf()
    private val messageLabel: MutableList<MessageLabel> = mutableListOf()

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
        this.sessionLabel.addAll(label)
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
        messageLabel.addAll(labels)
    }

    /**
     * Sets the conversation result.
     *
     * @param result displayed in Analytics section in JAICP Application Console and xls report.
     * */
    fun setSessionResult(result: String) {
        this.sessionResult = result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as JaicpAnalyticsAPI

        if (sessionResult != other.sessionResult) return false
        if (comment != other.comment) return false
        if (sessionData != other.sessionData) return false
        if (sessionLabel != other.sessionLabel) return false
        if (messageLabel != other.messageLabel) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sessionResult?.hashCode() ?: 0
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + sessionData.hashCode()
        result = 31 * result + sessionLabel.hashCode()
        result = 31 * result + messageLabel.hashCode()
        return result
    }

    @Serializable
    data class MessageLabel(
        val labelName: String,
        val groupName: String
    )
}
