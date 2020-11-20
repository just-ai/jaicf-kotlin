package com.justai.jaicf.channel.vk

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.helpers.logging.WithLogger
import com.vk.api.sdk.objects.Validable
import com.vk.api.sdk.objects.audio.Audio
import com.vk.api.sdk.objects.board.TopicComment
import com.vk.api.sdk.objects.callback.*
import com.vk.api.sdk.objects.callback.messages.CallbackMessage
import com.vk.api.sdk.objects.messages.Message
import com.vk.api.sdk.objects.photos.Photo
import com.vk.api.sdk.objects.video.Video
import com.vk.api.sdk.objects.wall.WallComment
import com.vk.api.sdk.objects.wall.Wallpost
import java.lang.reflect.Type


object VkBotRequestConverter : WithLogger {
    private const val CALLBACK_EVENT_MESSAGE_NEW = "message_new"
    private const val CALLBACK_EVENT_WALL_POST_NEW = "wall_post_new"


    private const val CALLBACK_EVENT_PHOTO_NEW = "photo_new"
    private const val CALLBACK_EVENT_PHOTO_COMMENT_NEW = "photo_comment_new"
    private const val CALLBACK_EVENT_PHOTO_COMMENT_EDIT = "photo_comment_edit"
    private const val CALLBACK_EVENT_PHOTO_COMMENT_RESTORE = "photo_comment_restore"
    private const val CALLBACK_EVENT_PHOTO_COMMENT_DELETE = "photo_comment_delete"
    private const val CALLBACK_EVENT_AUDIO_NEW = "audio_new"
    private const val CALLBACK_EVENT_VIDEO_NEW = "video_new"
    private const val CALLBACK_EVENT_VIDEO_COMMENT_NEW = "video_comment_new"
    private const val CALLBACK_EVENT_VIDEO_COMMENT_EDIT = "video_comment_edit"
    private const val CALLBACK_EVENT_VIDEO_COMMENT_RESTORE = "video_comment_restore"
    private const val CALLBACK_EVENT_VIDEO_COMMENT_DELETE = "video_comment_delete"
    private const val CALLBACK_EVENT_WALL_REPOST = "wall_repost"
    private const val CALLBACK_EVENT_WALL_REPLY_NEW = "wall_reply_new"
    private const val CALLBACK_EVENT_WALL_REPLY_EDIT = "wall_reply_edit"
    private const val CALLBACK_EVENT_WALL_REPLY_RESTORE = "wall_reply_restore"
    private const val CALLBACK_EVENT_WALL_REPLY_DELETE = "wall_reply_delete"
    private const val CALLBACK_EVENT_BOARD_POST_NEW = "board_post_new"
    private const val CALLBACK_EVENT_BOARD_POST_EDIT = "board_post_edit"
    private const val CALLBACK_EVENT_BOARD_POST_RESTORE = "board_post_restore"
    private const val CALLBACK_EVENT_BOARD_POST_DELETE = "board_post_delete"
    private const val CALLBACK_EVENT_MARKET_COMMENT_NEW = "market_comment_new"
    private const val CALLBACK_EVENT_MARKET_COMMENT_EDIT = "market_comment_edit"
    private const val CALLBACK_EVENT_MARKET_COMMENT_RESTORE = "market_comment_restore"
    private const val CALLBACK_EVENT_MARKET_COMMENT_DELETE = "market_comment_delete"
    private const val CALLBACK_EVENT_GROUP_LEAVE = "group_leave"
    private const val CALLBACK_EVENT_GROUP_JOIN = "group_join"
    private const val CALLBACK_EVENT_GROUP_CHANGE_SETTINGS = "group_change_settings"
    private const val CALLBACK_EVENT_GROUP_CHANGE_PHOTO = "group_change_photo"
    private const val CALLBACK_EVENT_GROUP_OFFICERS_EDIT = "group_officers_edit"
    private const val CALLBACK_EVENT_POLL_VOTE_NEW = "poll_vote_new"
    private const val CALLBACK_EVENT_USER_BLOCK = "user_block"
    private const val CALLBACK_EVENT_USER_UNBLOCK = "user_unblock"
    private const val CALLBACK_EVENT_CONFIRMATION = "confirmation"
    private const val MESSAGE_TYPING_STATE = "message_typing_state"

    private val gson: Gson = Gson()

    private val typesMap: Map<String, Type> = mapOf(
        CALLBACK_EVENT_MESSAGE_NEW to object : TypeToken<CallbackMessage<Message?>?>() {}.type,
        CALLBACK_EVENT_WALL_POST_NEW to object : TypeToken<CallbackMessage<Wallpost?>?>() {}.type,
        CALLBACK_EVENT_PHOTO_NEW to object : TypeToken<CallbackMessage<Photo?>?>() {}.type,

        CALLBACK_EVENT_PHOTO_COMMENT_NEW to object : TypeToken<CallbackMessage<PhotoComment?>?>() {}.type,
        CALLBACK_EVENT_PHOTO_COMMENT_EDIT to object : TypeToken<CallbackMessage<PhotoComment?>?>() {}.type,
        CALLBACK_EVENT_PHOTO_COMMENT_RESTORE to object : TypeToken<CallbackMessage<PhotoComment?>?>() {}.type,
        CALLBACK_EVENT_PHOTO_COMMENT_DELETE to object : TypeToken<CallbackMessage<PhotoCommentDelete?>?>() {}.type,
        CALLBACK_EVENT_AUDIO_NEW to object : TypeToken<CallbackMessage<Audio?>?>() {}.type,
        CALLBACK_EVENT_VIDEO_NEW to object : TypeToken<CallbackMessage<Video?>?>() {}.type,
        CALLBACK_EVENT_VIDEO_COMMENT_NEW to object : TypeToken<CallbackMessage<VideoComment?>?>() {}.type,
        CALLBACK_EVENT_VIDEO_COMMENT_EDIT to object : TypeToken<CallbackMessage<VideoComment?>?>() {}.type,
        CALLBACK_EVENT_VIDEO_COMMENT_RESTORE to object : TypeToken<CallbackMessage<VideoComment?>?>() {}.type,
        CALLBACK_EVENT_VIDEO_COMMENT_DELETE to object : TypeToken<CallbackMessage<VideoCommentDelete?>?>() {}.type,

        CALLBACK_EVENT_WALL_REPOST to object : TypeToken<CallbackMessage<Wallpost?>?>() {}.type,
        CALLBACK_EVENT_WALL_REPLY_NEW to object : TypeToken<CallbackMessage<WallComment?>?>() {}.type,
        CALLBACK_EVENT_WALL_REPLY_EDIT to object : TypeToken<CallbackMessage<WallComment?>?>() {}.type,
        CALLBACK_EVENT_WALL_REPLY_RESTORE to object : TypeToken<CallbackMessage<WallComment?>?>() {}.type,
        CALLBACK_EVENT_WALL_REPLY_DELETE to object : TypeToken<CallbackMessage<WallCommentDelete?>?>() {}.type,
        CALLBACK_EVENT_BOARD_POST_NEW to object : TypeToken<CallbackMessage<TopicComment?>?>() {}.type,
        CALLBACK_EVENT_BOARD_POST_EDIT to object : TypeToken<CallbackMessage<TopicComment?>?>() {}.type,
        CALLBACK_EVENT_BOARD_POST_RESTORE to object : TypeToken<CallbackMessage<TopicComment?>?>() {}.type,
        CALLBACK_EVENT_BOARD_POST_DELETE to object : TypeToken<CallbackMessage<BoardPostDelete?>?>() {}.type,
        CALLBACK_EVENT_MARKET_COMMENT_NEW to object : TypeToken<CallbackMessage<MarketComment?>?>() {}.type,
        CALLBACK_EVENT_MARKET_COMMENT_EDIT to object : TypeToken<CallbackMessage<MarketComment?>?>() {}.type,
        CALLBACK_EVENT_MARKET_COMMENT_RESTORE to object : TypeToken<CallbackMessage<MarketComment?>?>() {}.type,
        CALLBACK_EVENT_MARKET_COMMENT_DELETE to object : TypeToken<CallbackMessage<MarketCommentDelete?>?>() {}.type,
        CALLBACK_EVENT_GROUP_LEAVE to object : TypeToken<CallbackMessage<GroupLeave?>?>() {}.type,
        CALLBACK_EVENT_GROUP_JOIN to object : TypeToken<CallbackMessage<GroupJoin?>?>() {}.type,
        CALLBACK_EVENT_GROUP_CHANGE_SETTINGS to object : TypeToken<CallbackMessage<GroupChangeSettings?>?>() {}.type,
        CALLBACK_EVENT_GROUP_CHANGE_PHOTO to object : TypeToken<CallbackMessage<GroupChangePhoto?>?>() {}.type,
        CALLBACK_EVENT_GROUP_OFFICERS_EDIT to object : TypeToken<CallbackMessage<GroupOfficersEdit?>?>() {}.type,
        CALLBACK_EVENT_USER_BLOCK to object : TypeToken<CallbackMessage<UserBlock?>?>() {}.type,
        CALLBACK_EVENT_USER_UNBLOCK to object : TypeToken<CallbackMessage<UserUnblock?>?>() {}.type,
        CALLBACK_EVENT_POLL_VOTE_NEW to object : TypeToken<CallbackMessage<PollVoteNew?>?>() {}.type
    )


    fun convert(httpBotRequest: HttpBotRequest) = try {
        convert(gson.fromJson(httpBotRequest.receiveText(), JsonObject::class.java))
    } catch (e: Exception) {
        logger.warn("Failed to convert httpBotRequest to VkBotRequest: ", e)
        null
    }

    private fun convert(json: JsonObject): BotRequest? {
        val type = json["type"].asString
        val typeOfClass = typesMap[type]

        if (typeOfClass == null) {
            logger.info("Unknown update type received: $type")
            return null
        }

        val callbackMessage: CallbackMessage<*> = gson.fromJson(json, typeOfClass)
        return when (type) {
            CALLBACK_EVENT_MESSAGE_NEW -> with(callbackMessage.get<Message>()) {
                asAttachmentsEvent() ?: VkTextBotRequest(this)
            }
            CALLBACK_EVENT_WALL_POST_NEW -> with(callbackMessage.get<Wallpost>()) {
                asAttachmentsEvent() ?: VkWallPostBotRequest(this)
            }
            else -> VkGroupEvent(callbackMessage.get(), CALLBACK_EVENT_PHOTO_NEW)
/*            CALLBACK_EVENT_PHOTO_NEW -> photoNew(
                message.groupId,
                message.secret,
                message.getObject() as Photo
            )
            CALLBACK_EVENT_PHOTO_COMMENT_NEW -> photoCommentNew(
                message.groupId,
                message.secret,
                message.getObject() as PhotoComment
            )
            CALLBACK_EVENT_PHOTO_COMMENT_EDIT -> photoCommentEdit(
                message.groupId,
                message.secret,
                message.getObject() as PhotoComment
            )
            CALLBACK_EVENT_PHOTO_COMMENT_RESTORE -> photoCommentRestore(
                message.groupId,
                message.secret,
                message.getObject() as PhotoComment
            )
            CALLBACK_EVENT_PHOTO_COMMENT_DELETE -> photoCommentDelete(
                message.groupId,
                message.secret,
                message.getObject() as PhotoCommentDelete
            )
            CALLBACK_EVENT_AUDIO_NEW -> audioNew(
                message.groupId,
                message.secret,
                message.getObject() as Audio
            )
            CALLBACK_EVENT_VIDEO_NEW -> videoNew(
                message.groupId,
                message.secret,
                message.getObject() as Video
            )
            CALLBACK_EVENT_VIDEO_COMMENT_NEW -> videoCommentNew(
                message.groupId,
                message.secret,
                message.getObject() as VideoComment
            )
            CALLBACK_EVENT_VIDEO_COMMENT_EDIT -> videoCommentEdit(
                message.groupId,
                message.secret,
                message.getObject() as VideoComment
            )
            CALLBACK_EVENT_VIDEO_COMMENT_RESTORE -> videoCommentRestore(
                message.groupId,
                message.secret,
                message.getObject() as VideoComment
            )
            CALLBACK_EVENT_VIDEO_COMMENT_DELETE -> videoCommentDelete(
                message.groupId,
                message.secret,
                message.getObject() as VideoCommentDelete
            )
            CALLBACK_EVENT_WALL_REPOST -> wallRepost(
                message.groupId,
                message.secret,
                message.getObject() as Wallpost
            )
            CALLBACK_EVENT_WALL_REPLY_NEW -> wallReplyNew(
                message.groupId,
                message.secret,
                message.getObject() as WallComment
            )
            CALLBACK_EVENT_WALL_REPLY_EDIT -> wallReplyEdit(
                message.groupId,
                message.secret,
                message.getObject() as WallComment
            )
            CALLBACK_EVENT_WALL_REPLY_RESTORE -> wallReplyRestore(
                message.groupId,
                message.secret,
                message.getObject() as WallComment
            )
            CALLBACK_EVENT_WALL_REPLY_DELETE -> wallReplyDelete(
                message.groupId,
                message.secret,
                message.getObject() as WallCommentDelete
            )
            CALLBACK_EVENT_BOARD_POST_NEW -> boardPostNew(
                message.groupId,
                message.secret,
                message.getObject() as TopicComment
            )
            CALLBACK_EVENT_BOARD_POST_EDIT -> boardPostEdit(
                message.groupId,
                message.secret,
                message.getObject() as TopicComment
            )
            CALLBACK_EVENT_BOARD_POST_RESTORE -> boardPostRestore(
                message.groupId,
                message.secret,
                message.getObject() as TopicComment
            )
            CALLBACK_EVENT_BOARD_POST_DELETE -> boardPostDelete(
                message.groupId,
                message.secret,
                message.getObject() as BoardPostDelete
            )
            CALLBACK_EVENT_MARKET_COMMENT_NEW -> marketCommentNew(
                message.groupId,
                message.secret,
                message.getObject() as MarketComment
            )
            CALLBACK_EVENT_MARKET_COMMENT_EDIT -> marketCommentEdit(
                message.groupId,
                message.secret,
                message.getObject() as MarketComment
            )
            CALLBACK_EVENT_MARKET_COMMENT_RESTORE -> marketCommentRestore(
                message.groupId,
                message.secret,
                message.getObject() as MarketComment
            )
            CALLBACK_EVENT_MARKET_COMMENT_DELETE -> marketCommentDelete(
                message.groupId,
                message.secret,
                message.getObject() as MarketCommentDelete
            )
            CALLBACK_EVENT_GROUP_LEAVE -> groupLeave(
                message.groupId,
                message.secret,
                message.getObject() as GroupLeave
            )
            CALLBACK_EVENT_GROUP_JOIN -> groupJoin(
                message.groupId,
                message.secret,
                message.getObject() as GroupJoin
            )
            CALLBACK_EVENT_GROUP_CHANGE_SETTINGS -> groupChangeSettings(
                message.groupId,
                message.secret,
                message.getObject() as GroupChangeSettings
            )
            CALLBACK_EVENT_GROUP_CHANGE_PHOTO -> groupChangePhoto(
                message.groupId,
                message.secret,
                message.getObject() as GroupChangePhoto
            )
            CALLBACK_EVENT_GROUP_OFFICERS_EDIT -> groupOfficersEdit(
                message.groupId,
                message.secret,
                message.getObject() as GroupOfficersEdit
            )
            CALLBACK_EVENT_USER_BLOCK -> userBlock(
                message.groupId,
                message.secret,
                message.getObject() as UserBlock
            )
            CALLBACK_EVENT_USER_UNBLOCK -> userUnblock(
                message.groupId,
                message.secret,
                message.getObject() as UserUnblock
            )
            CALLBACK_EVENT_POLL_VOTE_NEW -> pollVoteNew(
                message.groupId,
                message.secret,
                message.getObject() as PollVoteNew
            )*/
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> CallbackMessage<*>.get() = `object` as T

private fun Message.asAttachmentsEvent(): EventBotRequest? {
    if (!text.isNullOrEmpty()) return null

    return null
}

private fun Wallpost.asAttachmentsEvent(): EventBotRequest? {
    if (!text.isNullOrEmpty()) return null

    return null
}

