package com.justai.jaicf.activator.llm.vectorstore

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.core.JsonField
import com.openai.core.MultipartField
import com.openai.models.files.FileCreateParams
import com.openai.models.files.FilePurpose
import com.openai.models.vectorstores.VectorStoreCreateParams
import com.openai.models.vectorstores.VectorStoreSearchParams
import com.openai.models.vectorstores.VectorStoreSearchResponse.Content.Type.Value.TEXT
import com.openai.models.vectorstores.files.FileListParams
import com.openai.models.vectorstores.files.FileCreateParams.Attributes
import java.io.InputStream
import java.nio.file.Path

class OpenAIVectorStore(
    val vectorStoreId: String,
    val params: VectorStoreSearchParams.Builder = VectorStoreSearchParams.builder(),
    val filters: VectorStoreSearchParams.Filters? = null,
    val client: OpenAIClient = OpenAIOkHttpClient.fromEnv(),
) : LLMVectorStore {
    override suspend fun search(request: LLMVectorStore.Request) = LLMVectorStore.Response(
        client.vectorStores().search(
            params
                .vectorStoreId(vectorStoreId)
                .query(request.query)
                .filters(JsonField.ofNullable(filters))
                .build()
        ).data()
            .filter { c -> c.content().any { it.type().value() == TEXT } }
            .map { c ->
                LLMVectorStore.Response.Chunk(
                c.score(),
                c.content().joinToString("\n") { it.text() },
                LLMVectorStore.Response.Source(c.fileId(), c.filename()),
                )
            }
    )

    fun addFile(fileId: String, attributes: Attributes?) = client.vectorStores().files().create(
        com.openai.models.vectorstores.files.FileCreateParams.builder()
            .vectorStoreId(vectorStoreId)
            .fileId(fileId)
            .attributes(attributes)
            .build()
    )

    fun deleteFile(fileId: String) = client.vectorStores().files().delete(
        com.openai.models.vectorstores.files.FileDeleteParams.builder()
            .vectorStoreId(vectorStoreId)
            .fileId(fileId)
            .build()
    )

    fun uploadFile(file: InputStream, name: String, attributes: Attributes? = null) = client.files().create(
        FileCreateParams.builder()
            .purpose(FilePurpose.ASSISTANTS)
            .file(MultipartField.builder<InputStream>()
                .filename(name)
                .value(file)
                .build()
            ).build()
    ).let { addFile(it.id(), attributes) }

    fun uploadFile(file: Path, attributes: Attributes? = null) = client.files().create(
        FileCreateParams.builder()
            .file(file)
            .purpose(FilePurpose.ASSISTANTS)
            .build()
    ).let { addFile(it.id(), attributes) }

    fun listFiles(params: FileListParams = FileListParams.none()) =
        client.vectorStores().files().list(vectorStoreId, params)
}

class OpenAIVectorStoreFactory(
    val client: OpenAIClient = OpenAIOkHttpClient.fromEnv()
) {
    fun create(
        name: String,
        params: VectorStoreSearchParams.Builder = VectorStoreSearchParams.builder(),
        filters: VectorStoreSearchParams.Filters? = null,
    ) = client.vectorStores().create(VectorStoreCreateParams.builder().name(name).build()).let {
        OpenAIVectorStore(it.id(), params, filters, client)
    }

    fun delete(vectorStoreId: String) {
        client.vectorStores().delete(vectorStoreId)
    }
}