package com.justai.jaicf.channel.telegram

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Universal message aggregator for Telegram requests.
 *
 * Implements smart aggregation with two modes:
 * 1. **Media Group Mode**: Uses Telegram's `mediaGroupId` for instant grouping (no delay)
 * 2. **Debounce Mode**: Uses time-based debouncing for sequential messages
 *
 * Supports cross-type aggregation (text + photos + videos) into composite requests.
 *
 * @param waitTimeMs Debounce delay in milliseconds (default: 500ms)
 * @param scope Coroutine scope for async operations
 * @param strategy Aggregation strategy for custom logic (default: DefaultAggregationStrategy)
 * @param maxItems Maximum number of items to aggregate (prevents abuse)
 * @param useMediaGroupId Whether to use Telegram's mediaGroupId for instant grouping
 */
class TelegramRequestAggregator(
    private val waitTimeMs: Long = DEFAULT_WAIT_TIME_MS,
    private val scope: CoroutineScope,
    private val strategy: AggregationStrategy = DefaultAggregationStrategy(),
    private val maxItems: Int = DEFAULT_MAX_ITEMS,
    private val useMediaGroupId: Boolean = true
) {
    private val mutex = Mutex()
    private val pendingRequests = mutableMapOf<AggregationKey, PendingRequests>()

    companion object {
        const val DEFAULT_WAIT_TIME_MS = 500L
        const val DEFAULT_MAX_ITEMS = 20
    }

    /**
     * Key for aggregation grouping.
     * Groups by chat ID and either mediaGroupId (for Telegram media groups) or null (for sequential messages).
     */
    private data class AggregationKey(
        val chatId: Long,
        val mediaGroupId: String?
    )

    /**
     * Container for pending requests awaiting aggregation.
     */
    private data class PendingRequests(
        val requests: MutableList<TelegramBotRequest>,
        val onProcess: suspend (TelegramBotRequest) -> Unit,
        var job: Job? = null
    )

    /**
     * Adds a request to the aggregator.
     *
     * The request will either:
     * - Be processed immediately if it shouldn't be aggregated
     * - Be grouped with mediaGroupId requests (instant, no delay)
     * - Be debounced with sequential messages (delay-based)
     *
     * @param request The incoming Telegram request
     * @param onProcess Callback to process the final (possibly aggregated) request
     */
    suspend fun addRequest(
        request: TelegramBotRequest,
        onProcess: suspend (TelegramBotRequest) -> Unit
    ) {
        scope.launch {
            mutex.withLock {
                // Extract mediaGroupId if available and enabled
                val mediaGroupId = if (useMediaGroupId) request.message.mediaGroupId else null

                // Determine aggregation key
                val key = AggregationKey(
                    chatId = request.chatId,
                    mediaGroupId = mediaGroupId
                )

                // Get or create pending requests for this key
                val pending = pendingRequests.getOrPut(key) {
                    PendingRequests(mutableListOf(), onProcess)
                }

                // Check if we should aggregate using the strategy
                if (!strategy.shouldAggregate(request.chatId, request, pending.requests)) {
                    // Strategy says no - process immediately
                    onProcess(request)
                    return@launch
                }

                // Check max items limit
                if (pending.requests.size >= maxItems) {
                    // Hit limit - process what we have and start fresh
                    processAndRemove(key)
                    // Add current request to new batch
                    pendingRequests[key] = PendingRequests(
                        mutableListOf(request),
                        onProcess
                    )
                    scheduleProcessing(key, mediaGroupId != null)
                    return@launch
                }

                // Add request to pending batch
                pending.requests.add(request)

                // Cancel existing job (restart debounce timer)
                pending.job?.cancel()

                // Schedule processing
                scheduleProcessing(key, mediaGroupId != null)
            }
        }.join() // Wait for completion to ensure sequential processing
    }

    /**
     * Schedules processing of pending requests.
     *
     * @param key The aggregation key
     * @param isMediaGroup If true, uses shorter delay for media groups
     */
    private fun scheduleProcessing(key: AggregationKey, isMediaGroup: Boolean) {
        val pending = pendingRequests[key] ?: return

        pending.job = scope.launch {
            // Media groups: very short delay to collect all items sent together
            // Regular messages: full debounce delay
            val delay = if (isMediaGroup) 50L else waitTimeMs
            delay(delay)
            processAndRemove(key)
        }
    }

    /**
     * Processes pending requests and removes them from the queue.
     */
    private suspend fun processAndRemove(key: AggregationKey) {
        val pending = mutex.withLock {
            pendingRequests.remove(key)
        } ?: return

        if (pending.requests.isEmpty()) return

        // Use strategy to create final request
        val finalRequest = strategy.createComposite(pending.requests)

        // Process the result
        pending.onProcess(finalRequest)
    }

    /**
     * Clears all pending requests. Useful for testing or shutdown.
     */
    suspend fun clear() {
        mutex.withLock {
            pendingRequests.values.forEach { it.job?.cancel() }
            pendingRequests.clear()
        }
    }
}
