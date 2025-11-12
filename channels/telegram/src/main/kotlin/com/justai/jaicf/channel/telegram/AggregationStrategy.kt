package com.justai.jaicf.channel.telegram

/**
 * Strategy interface for customizing message aggregation behavior.
 * Allows advanced users to control when and how messages should be aggregated.
 */
interface AggregationStrategy {
    /**
     * Determines whether a new request should be aggregated with pending requests.
     *
     * @param chatId The chat ID where the message originated
     * @param newRequest The new incoming request
     * @param pendingRequests List of requests currently pending aggregation
     * @return true if the new request should be aggregated, false to process immediately
     */
    suspend fun shouldAggregate(
        chatId: Long,
        newRequest: TelegramBotRequest,
        pendingRequests: List<TelegramBotRequest>
    ): Boolean

    /**
     * Creates a composite request from a list of aggregated requests.
     *
     * @param requests List of requests to combine
     * @return A composite request or a single request if only one item
     */
    fun createComposite(requests: List<TelegramBotRequest>): TelegramBotRequest
}

/**
 * Default aggregation strategy that aggregates all messages within the time window.
 * This is the standard behavior suitable for most use cases.
 */
class DefaultAggregationStrategy : AggregationStrategy {
    override suspend fun shouldAggregate(
        chatId: Long,
        newRequest: TelegramBotRequest,
        pendingRequests: List<TelegramBotRequest>
    ): Boolean = true // Always aggregate within time window

    override fun createComposite(requests: List<TelegramBotRequest>): TelegramBotRequest {
        // Single request - return as-is
        if (requests.size == 1) {
            return requests.first()
        }

        // Multiple requests - create composite
        return TelegramCompositeRequest(
            update = requests.first().update,
            message = requests.first().message,
            items = requests.map { it.toMessageItem() },
            mediaGroupId = requests.first().message.mediaGroupId
        )
    }
}

/**
 * Smart aggregation strategy that excludes bot commands from aggregation.
 * Commands (messages starting with '/') are processed immediately.
 *
 * Useful for bots that need instant command response while still benefiting
 * from message aggregation for regular messages.
 */
class CommandAwareAggregationStrategy : AggregationStrategy {
    override suspend fun shouldAggregate(
        chatId: Long,
        newRequest: TelegramBotRequest,
        pendingRequests: List<TelegramBotRequest>
    ): Boolean {
        // Don't aggregate if new message is a command
        if (newRequest is TelegramTextRequest && newRequest.input.trimStart().startsWith("/")) {
            return false
        }

        // Don't aggregate if there are already too many pending
        if (pendingRequests.size >= 10) {
            return false
        }

        return true
    }

    override fun createComposite(requests: List<TelegramBotRequest>): TelegramBotRequest {
        if (requests.size == 1) {
            return requests.first()
        }

        return TelegramCompositeRequest(
            update = requests.first().update,
            message = requests.first().message,
            items = requests.map { it.toMessageItem() },
            mediaGroupId = requests.first().message.mediaGroupId
        )
    }
}
