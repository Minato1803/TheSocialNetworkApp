package com.datn.thesocialnetwork.data.repository.model.chat_room

import com.google.firebase.database.DatabaseReference

data class RoomMember(
    var userId: String? = null,
    var avatarUrl: String? = null,
    var joinDate: Long? = null,
    var name:String?=null
) {
    var id : String = "" // key
    val toHashMap: HashMap<String, Any?>
        get() = hashMapOf(
            FIELD_ID to userId,
            FIELD_AVATAR_URL to avatarUrl,
            FIELD_JOIN_DATE to joinDate,
            FIELD_NAME to name,
        )

    companion object{
        const val FIELD_ID = "userId"
        const val FIELD_AVATAR_URL = "avatarUrl"
        const val FIELD_JOIN_DATE = "joinDate"
        const val FIELD_NAME = "name"
    }
}