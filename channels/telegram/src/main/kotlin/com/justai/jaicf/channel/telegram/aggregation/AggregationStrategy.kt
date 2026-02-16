package com.justai.jaicf.channel.telegram.aggregation

import com.justai.jaicf.channel.telegram.*

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
     * Default implementation copies the first request with the aggregated list.
     *
     * @param requests List of requests to combine
     * @return A composite request or a single request if only one item
     */
    fun createComposite(requests: List<TelegramBotRequest>): TelegramBotRequest {
        // Single request - return as-is
        if (requests.size == 1) {
            return requests.first()
        }

        // Multiple requests - copy first request with aggregated list
        return when (val first = requests.first()) {
            is TelegramTextRequest -> first.copy(aggregated = requests)
            is TelegramPhotosRequest -> first.copy(aggregated = requests)
            is TelegramVideoRequest -> first.copy(aggregated = requests)
            is TelegramDocumentRequest -> first.copy(aggregated = requests)
            is TelegramAudioRequest -> first.copy(aggregated = requests)
            is TelegramVoiceRequest -> first.copy(aggregated = requests)
            is TelegramVideoNoteRequest -> first.copy(aggregated = requests)
            is TelegramStickerRequest -> first.copy(aggregated = requests)
            is TelegramAnimationRequest -> first.copy(aggregated = requests)
            is TelegramLocationRequest -> first.copy(aggregated = requests)
            is TelegramContactRequest -> first.copy(aggregated = requests)
            is TelegramGameRequest -> first.copy(aggregated = requests)
            is TelegramQueryRequest -> first.copy(aggregated = requests)
            else -> first // Fallback for other types
        }
    }
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
}
