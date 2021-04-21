package com.justai.jaicf.channel.vk.api

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.objects.docs.GetMessagesUploadServerType
import com.vk.api.sdk.objects.docs.responses.SaveResponse
import java.io.File
import java.util.concurrent.ConcurrentHashMap


/**
 * In-memory implementation of [VkReactionsContentStorage] to persist image/document or audio identifiers sent by reactions.
 * */
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
        type: GetMessagesUploadServerType,
        identityKeyProvider: (SaveResponse) -> String
    ): String = uploadedUrlResourcesMap.getOrPut(url) {
        identityKeyProvider(
            uploadDocument(api, groupActor, peerId, uploadResourceToFile(url, type.value, "doc"), type)
        )
    }

    override fun getOrUploadFile(
        api: VkApiClient,
        groupActor: GroupActor,
        peerId: Int,
        file: File,
        type: GetMessagesUploadServerType,
        identityKeyProvider: (SaveResponse) -> String
    ): String = uploadedFileResourcesMap.getOrPut(file) {
        identityKeyProvider(uploadDocument(api, groupActor, peerId, file, type))
    }
}