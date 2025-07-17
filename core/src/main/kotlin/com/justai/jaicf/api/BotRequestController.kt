package com.justai.jaicf.api

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin

private typealias OnCompleteHandler = (cause: Throwable?) -> Unit

/**
 * Class for listening and cancelling request processing.
 * Pass optional [BotRequestController] to [BotApi.process] and then listen for completion or cancel a request.
 * Note that cancelling doesn't guarantee that a request interrupts immediately. It depends on how the internal scenario logic is implemented.
 *
 * @param onCompleteHandler handler that will be invoked once the request is complete.
 *
 * @see BotApi
 */
class BotRequestController(
    private val onCompleteHandler: OnCompleteHandler? = null,
) {
    private lateinit var job: Job

    private fun setupOnCompleteHandler() {
        if (onCompleteHandler != null && ::job.isInitialized) {
            job.invokeOnCompletion(onCompleteHandler)
        }
    }

    internal fun setJob(job: Job) {
        this.job = job
        setupOnCompleteHandler()
    }

    val isActive
        get() = ::job.isInitialized && job.isActive

    suspend fun cancelAndJoin() {
        if (::job.isInitialized && !job.isCancelled && !job.isCompleted) {
            job.cancelAndJoin()
        }
    }

    fun cancel() {
        if (::job.isInitialized) {
            job.cancel()
        }
    }
}