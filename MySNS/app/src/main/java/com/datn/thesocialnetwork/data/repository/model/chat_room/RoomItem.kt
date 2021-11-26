package com.datn.thesocialnetwork.data.repository.model.chat_room

import java.util.HashMap

abstract class RoomItem(open var roomId: String? = null,
                        open var name: String? = null,
                        open var avatarUrl: String? = null,
                        open var lastMsg: String? = null,
                        open var lastMsgTime: Long? = null,
                        open var unseenMsgNum: Int = 0,
                        open var roomType: Int,
                        open var lastSenderId: String? = null,
                        open var lastSenderName: String? = null,//todo: change this
                        open var lastTypingMember: RoomMember? = null
) {

    open val toHashMap: HashMap<String, Any?>
        get() = hashMapOf(
            FIELD_NAME to name,
            FIELD_AVATAR_URL to avatarUrl,
            FIELD_LAST_MSG to lastMsg,
            FIELD_LAST_MSG_TIME to lastMsgTime,
            FIELD_UNSEEN_MSG_NUM to unseenMsgNum,
            FIELD_ROOM_TYPE to roomType,
            FIELD_LAST_SENDER_ID to lastSenderId,
            FIELD_LAST_SENDER_NAME to lastSenderName,
        )

    companion object {
        const val PAYLOAD_NEW_MESSAGE = 0
        const val PAYLOAD_SEEN_MESSAGE = 1
        const val PAYLOAD_ONLINE_STATUS = 2
        const val PAYLOAD_TYPING = 3
        const val PAYLOAD_SELECT = 4

        const val FIELD_AVATAR_URL = "avatarUrl"
        const val FIELD_LAST_MSG = "lastMsg"
        const val FIELD_LAST_MSG_TIME = "lastMsgTime"
        //        const val FIELD_LAST_MSG_TYPE = "lastMsgType"
        const val FIELD_NAME = "name"
        const val FIELD_UNSEEN_MSG_NUM = "unseenMsgNum"
        const val FIELD_ROOM_TYPE = "roomType"
        const val FIELD_LAST_SENDER_ID = "lastSenderId"
        const val FIELD_LAST_SENDER_NAME = "lastSenderName"
    }
}