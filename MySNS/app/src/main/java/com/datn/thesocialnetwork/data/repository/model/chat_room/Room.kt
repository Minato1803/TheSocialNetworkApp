package com.datn.thesocialnetwork.data.repository.model.chat_room

import com.datn.thesocialnetwork.data.repository.UserRepository

abstract class Room(
    open var roomId: String? = null,
    open var avatarUrl: String? = null,
    open var createdTime: Long? = null,
    open var name: String? = null,
    open var memberMap: Map<String, RoomMember>? = null,
    open var type: Int = TYPE_PEER,
) {
    open val toHashMap: HashMap<String, Any?>
        get() = hashMapOf(
            FIELD_CREATED_TIME to createdTime,
            FIELD_TYPE to type,
        )

    companion object {
        const val FIELD_CREATED_TIME = "createdTime"
        const val FIELD_TYPE = "type"
        const val FIELD_TYPING_MEMBERS = "typingMembers"
        const val FIELD_SEEN_MEMBERS_ID = "seenMembersId"

        const val TYPE_PEER = 0
        const val TYPE_GROUP = 1
    }
}