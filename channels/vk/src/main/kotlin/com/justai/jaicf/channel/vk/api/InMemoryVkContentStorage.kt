package com.justai.jaicf.channel.vk.api

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.objects.docs.responses.SaveResponse
import com.vk.api.sdk.objects.enums.DocsType
import org.apache.commons.io.FilenameUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import java.io.File
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

private val httpClient = HttpClientBuilder.create().build()

object InMemoryVkContentStorage : VkReactionsContentStorage {
    private val uploadedUrlResourcesMap = ConcurrentHashMap<String, String>()
    private val uploadedFileResourcesMap = ConcurrentHashMap<File, String>()

    override fun getOrUploadImage(api: VkApiClient, actor: GroupActor, peerId: Int, url: String): String =
        uploadedUrlResourcesMap.getOrPut(url) {
            uploadPhoto(api, actor, uploadResourceToFile(url, "image", "jpeg"))
        }

    override fun getOrUploadImage(api: VkApiClient, actor: GroupActor, peerId: Int, file: File): String =
        uploadedFileResourcesMap.getOrPut(file) {
            uploadPhoto(api, actor, file)
        }

    override fun getOrUploadUrl(
        api: VkApiClient,
        groupActor: GroupActor,
        peerId: Int,
        url: String,
        type: DocsType,
        transformer: (SaveResponse) -> String
    ): String = uploadedUrlResourcesMap.getOrPut(url) {
        transformer(
            uploadDocument(api, groupActor, peerId, uploadResourceToFile(url, type.value, "doc"), type)
        )
    }

    override fun getOrUploadFile(
        api: VkApiClient,
        groupActor: GroupActor,
        peerId: Int,
        file: File,
        type: DocsType,
        transformer: (SaveResponse) -> String
    ): String = uploadedFileResourcesMap.getOrPut(file) {
        transformer(uploadDocument(api, groupActor, peerId, file, type))
    }
}

private fun String.orIfEmpty(other: String) = if (isNullOrEmpty()) other else this

private fun uploadResourceToFile(url: String, prefix: String, defaultExtension: String): File {
    val ext = FilenameUtils.getExtension(url).orIfEmpty(defaultExtension)
    val file = File.createTempFile(prefix, "vk_upload.$ext")
    httpClient.execute(HttpGet(URI(url))).use {
        file.writeBytes(it.entity.content.readBytes())
    }
    return file
}

private fun uploadPhoto(
    vkApiClient: VkApiClient,
    groupActor: GroupActor,
    file: File
): String {
    val uploadUrl = vkApiClient.photos().getMessagesUploadServer(groupActor).execute().uploadUrl.toString()
    val uploadResponse = vkApiClient.upload().photoMessage(uploadUrl, file).execute()
    val photo = vkApiClient.photos().saveMessagesPhoto(groupActor, uploadResponse.photo)
        .server(uploadResponse.server)
        .hash(uploadResponse.hash).execute()
        .first()
    return "photo${photo.ownerId}_${photo.id}"
}

private fun uploadDocument(
    vkApiClient: VkApiClient,
    groupActor: GroupActor,
    peerId: Int,
    file: File,
    type: DocsType
): SaveResponse {
    val uploadUrl = vkApiClient.docs().getMessagesUploadServer(groupActor)
        .type(type)
        .peerId(peerId)
        .execute().uploadUrl.toString()
    val docUploadResponse = vkApiClient.upload().doc(uploadUrl, file).execute()
    return vkApiClient.docs().save(groupActor, docUploadResponse.file).execute()
}