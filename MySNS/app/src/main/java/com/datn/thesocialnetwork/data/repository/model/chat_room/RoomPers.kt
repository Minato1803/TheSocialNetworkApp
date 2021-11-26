package com.datn.thesocialnetwork.data.repository.model.chat_room

data class RoomPers(
    override var roomId: String? = null,
    override var avatarUrl: String? = null,
    override var createdTime: Long? = null,
    override var name: String? = null,
    override var memberMap: Map<String, RoomMember>? = null,
    var persId:String?=null
):Room(roomId, avatarUrl, createdTime, name, memberMap, TYPE_PEER) {
    override val toHashMap: HashMap<String, Any?>
        get() = hashMapOf(
            FIELD_PEER_ID to persId,
        )

    companion object {
        const val FIELD_PEER_ID = "persId"
    }
}