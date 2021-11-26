package com.datn.thesocialnetwork.data.repository.model.chat_room


data class RoomGroup(
    override var roomId: String? = null,
    override var avatarUrl: String? = null,
    override var createdTime: Long? = null,
    override var name: String? = null,
    override var memberMap: Map<String, RoomMember>? = null
):Room(roomId, avatarUrl, createdTime, name, memberMap, TYPE_GROUP)