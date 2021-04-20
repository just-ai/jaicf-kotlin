package com.justai.jaicf.channel.vk.api

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.objects.docs.responses.SaveResponse
import com.vk.api.sdk.objects.enums.DocsType
import java.io.File

interface VkReactionsContentStorage {

    fun getOrUploadImage(
        api: VkApiClient,
        actor: GroupActor,
        peerId: Int,
        url: String
    ): String

    fun getOrUploadImage(
        api: VkApiClient,
        actor: GroupActor,
        peerId: Int,
        file: File
    ): String

    fun getOrUploadUrl(
        api: VkApiClient,
        groupActor: GroupActor,
        peerId: Int,
        url: String,
        type: DocsType,
        transformer: SaveResponse.() -> String
    ): String

    fun getOrUploadFile(
        api: VkApiClient,
        groupActor: GroupActor,
        peerId: Int,
        file: File,
        type: DocsType,
        transformer: SaveResponse.() -> String
    ): String?
}