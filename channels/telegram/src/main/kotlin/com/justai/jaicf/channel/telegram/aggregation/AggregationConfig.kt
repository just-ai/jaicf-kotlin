package com.justai.jaicf.channel.telegram.aggregation

data class AggregationConfig(
    val waitTimeMs: Long = DEFAULT_WAIT_TIME_MS,
    val useMediaGroupId: Boolean = true,
    val strategy: AggregationStrategy = DefaultAggregationStrategy(),
    val maxItems: Int = DEFAULT_MAX_ITEMS
) {
    companion object {
        const val DEFAULT_WAIT_TIME_MS = 500L
        const val DEFAULT_MAX_ITEMS = 20
    }
}