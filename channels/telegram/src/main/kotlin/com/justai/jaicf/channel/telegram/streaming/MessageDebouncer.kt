package com.justai.jaicf.channel.telegram.streaming

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Debounces message updates to avoid overwhelming the Telegram API with rapid updates.
 * Cancels pending updates when new updates arrive, executing only the most recent one after the debounce delay.
 *
 * @property debounceMs the delay in milliseconds before executing the action
 * @property scope the coroutine scope in which to execute the debounced actions
 */
class MessageDebouncer(
    private val debounceMs: Long,
    private val scope: CoroutineScope
) {
    private var pendingJob: Job? = null

    /**
     * Schedules an action to be executed after the debounce delay.
     * If called again before the delay expires, the previous action is cancelled.
     *
     * @param action the suspend function to execute after the debounce delay
     */
    fun debounce(action: suspend () -> Unit) {
        pendingJob?.cancel()
        pendingJob = scope.launch {
            delay(debounceMs)
            action()
        }
    }

    /**
     * Waits for any pending debounced action to complete.
     * Should be called to ensure all updates are sent before finishing stream processing.
     */
    suspend fun flush() {
        pendingJob?.join()
    }
}
