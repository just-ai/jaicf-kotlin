package com.justai.jaicf.channel.telegram.helpers

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException

fun downloadFileByUrl(url: String, fileName: String? = null): File {
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    return client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw IOException("Unexpected code $response")
        }

        val responseBody = response.body ?: throw IOException("Empty response body")

        val finalFileName = fileName ?: run {
            val urlFileName = url.substringAfterLast('/').takeIf { it.isNotBlank() }
            val contentDisposition = response.header("Content-Disposition")
            val headerFileName = contentDisposition?.let {
                if (it.contains("filename=")) {
                    it.substringAfter("filename=").trim('"')
                } else null
            }

            headerFileName ?: urlFileName ?: "downloaded_file"
        }

        val tempDir = System.getProperty("java.io.tmpdir")
        val tempFile = File(tempDir, finalFileName)

        tempFile.sink().buffer().use { sink ->
            sink.writeAll(responseBody.source())
        }

        tempFile
    }
}