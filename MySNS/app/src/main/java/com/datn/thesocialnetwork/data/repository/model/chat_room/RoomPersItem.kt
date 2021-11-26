package com.datn.thesocialnetwork.data.repository.model.chat_room

data class RoomPersItem (
    override var roomId: String? = null,
    override var name: String? = null,
    override var avatarUrl: String? = null,
    override var lastMsg: String? = null,
    override var lastMsgTime: Long? = null,
    override var unseenMsgNum: Int = 0,
    override var lastSenderId: String? = null,
    override var lastSenderName: String? = null,
    override var lastTypingMember: RoomMember? = null,
    var persId: String? = null,
    var lastOnlineTime: Long? = System.currentTimeMillis()
) : RoomItem(
    roomId,
    name,
    avatarUrl,
    lastMsg,
    lastMsgTime,
    unseenMsgNum,
    Room.TYPE_PEER,
    lastSenderId,
    lastSenderName,
    lastTypingMember
) {
    override val toHashMap: HashMap<String, Any?>
        get() = hashMapOf(
            FIELD_PEER_ID to persId,
        )
    companion object {
        const val FIELD_PEER_ID = "persId"
        const val FIELD_LAST_ONLINE_TIME = "lastOnlineTime"
    }
}