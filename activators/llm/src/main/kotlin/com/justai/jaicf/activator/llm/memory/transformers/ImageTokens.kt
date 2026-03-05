package com.justai.jaicf.activator.llm.memory.transformers

import com.openai.models.chat.completions.ChatCompletionContentPartImage
import okhttp3.OkHttpClient
import kotlin.jvm.optionals.getOrNull
import okhttp3.Request
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import kotlin.math.ceil
import kotlin.math.min

// Fallback token counts when dimensions cannot be determined
private const val IMAGE_TOKENS_FALLBACK_LOW = 85
private const val IMAGE_TOKENS_FALLBACK_HIGH = 765
private val defaultHttpClient by lazy { OkHttpClient() }
private val dimensionCache = ConcurrentHashMap<String, Pair<Int, Int>>()

/**
 * Fetches image dimensions from a URL, with successful results cached by URL.
 * Failures are not cached so transient errors (network, timeout) are retried next time.
 * Returns null on any failure.
 */
internal fun fetchImageDimensions(url: String): Pair<Int, Int>? {
    dimensionCache[url]?.let { return it }
    return try {
        val request = Request.Builder().url(url).build()
        defaultHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val stream: InputStream = response.body?.byteStream() ?: return null
            val image = ImageIO.read(stream) ?: return null
            Pair(image.width, image.height).also { dimensionCache[url] = it }
        }
    } catch (_: Exception) {
        null
    }
}

/**
 * Calculates OpenAI image token cost.
 *
 * - low detail: always 85 tokens
 * - high/auto detail: image is scaled to fit 2048×2048, then shortest side scaled to 768px,
 *   then divided into 512×512 tiles. Cost = 85 + 170 × tiles.
 *
 * Falls back to fixed estimate if dimensions are unavailable.
 */
private fun openAiImageTokens(url: String, isLowDetail: Boolean): Int {
    if (isLowDetail) return IMAGE_TOKENS_FALLBACK_LOW

    val (w, h) = fetchImageDimensions(url) ?: return IMAGE_TOKENS_FALLBACK_HIGH

    // Scale to fit within 2048×2048
    val scale1 = min(1.0, min(2048.0 / w, 2048.0 / h))
    val w1 = (w * scale1).toInt()
    val h1 = (h * scale1).toInt()

    // Scale so shortest side is 768px (only upscale if needed)
    val scale2 = 768.0 / min(w1, h1)
    val w2 = if (scale2 < 1.0) (w1 * scale2).toInt() else w1
    val h2 = if (scale2 < 1.0) (h1 * scale2).toInt() else h1

    val tilesX = ceil(w2 / 512.0).toInt()
    val tilesY = ceil(h2 / 512.0).toInt()
    val tiles = tilesX * tilesY

    return 85 + 170 * tiles
}

/**
 * Calculates Anthropic image token cost: (width × height) / 750, minimum 1.
 *
 * Falls back to fixed estimate if dimensions are unavailable.
 */
private fun anthropicImageTokens(url: String): Int {
    val (w, h) = fetchImageDimensions(url) ?: return IMAGE_TOKENS_FALLBACK_HIGH
    return maxOf(1, ceil(w.toDouble() * h / 750.0).toInt())
}

/**
 * Returns the token cost for an image URL, dispatching to the appropriate formula based on model name.
 * - Anthropic (claude-*): (width × height) / 750
 * - OpenAI / others: tiled cost based on OpenAI vision pricing
 *
 * Falls back to fixed estimates if dimensions cannot be fetched.
 */
fun ChatCompletionContentPartImage.countTokens(modelName: String?): Int {
    val imageUrl = imageUrl()
    val isLowDetail = imageUrl.detail().getOrNull() == ChatCompletionContentPartImage.ImageUrl.Detail.LOW
    return if (modelName != null && modelName.startsWith("claude-", ignoreCase = true)) {
        anthropicImageTokens(imageUrl.url())
    } else {
        openAiImageTokens(imageUrl.url(), isLowDetail)
    }
}
