package com.justai.jaicf.channel.vk.api

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.objects.docs.GetMessagesUploadServerType
import com.vk.api.sdk.objects.docs.responses.SaveResponse
import org.apache.commons.io.FilenameUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import java.io.File
import java.net.URI

/**
 * Defines a storage to save image, document or audio identifiers to send in VK Reactions.
 *
 * @see InMemoryVkContentStorage in memory storage implementation.
 * */
interface VkReactionsContentStorage {

    /**
     * Get image from storage by url or upload it to VK servers and return String-identifier to send in reaction
     * */
    fun getOrUploadImage(
        api: VkApiClient,
        actor: GroupActor,
        peerId: Int,
        url: String
    ): String

    /**
     * Get image from storage by file or upload it to VK servers and return String-identifier to send in reaction
     * */
    fun getOrUploadImage(
        api: VkApiClient,
        actor: GroupActor,
        peerId: Int,
        file: File
    ): String

    /**
     * Get document from storage by url or upload it to VK servers and return String-identifier to send in reaction
     * */
    fun getOrUploadUrl(
        api: VkApiClient,
        groupActor: GroupActor,
        peerId: Int,
        url: String,
        type: GetMessagesUploadServerType,
        identityKeyProvider: SaveResponse.() -> String
    ): String

    /**
     * Get document from storage by file or upload it to VK servers and return String-identifier to send in reaction
     * */
    fun getOrUploadFile(
        api: VkApiClient,
        groupActor: GroupActor,
        peerId: Int,
        file: File,
        type: GetMessagesUploadServerType,
        identityKeyProvider: SaveResponse.() -> String
    ): String?
}

private val httpClient = HttpClientBuilder.create().build()

private fun String.orIfEmpty(other: String) = if (isNullOrEmpty()) other else this

fun VkReactionsContentStorage.uploadResourceToFile(url: String, prefix: String, defaultExtension: String): File {
    val ext = FilenameUtils.getExtension(url).orIfEmpty(defaultExtension)
    val file = File.createTempFile(prefix, "vk_upload.$ext")
    httpClient.execute(HttpGet(URI(url))).use {
        file.writeBytes(it.entity.content.readBytes())
    }
    return file
}

fun VkReactionsContentStorage.uploadPhoto(
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

fun VkReactionsContentStorage.uploadDocument(
    vkApiClient: VkApiClient,
    groupActor: GroupActor,
    peerId: Int,
    file: File,
    type: GetMessagesUploadServerType
): SaveResponse {
    val uploadUrl = vkApiClient.docs().getMessagesUploadServer(groupActor)
        .type(type)
        .peerId(peerId)
        .execute().uploadUrl.toString()
    val docUploadResponse = vkApiClient.upload().doc(uploadUrl, file).execute()
    return vkApiClient.docs().save(groupActor, docUploadResponse.file).execute()
}