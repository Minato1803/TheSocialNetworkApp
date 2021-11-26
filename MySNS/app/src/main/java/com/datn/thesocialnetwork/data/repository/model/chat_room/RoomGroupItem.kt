package com.datn.thesocialnetwork.data.repository.model.chat_room

data class RoomGroupItem (
    override var roomId: String? = null,
    override var name: String? = null,
    override var avatarUrl: String? = null,
    override var lastMsg: String? = null,
    override var lastMsgTime: Long? = null,
    override var unseenMsgNum: Int = 0,
    override var lastSenderId: String? = null,
    override var lastSenderName: String? = null,//todo: change this
    override var lastTypingMember: RoomMember? = null
) : RoomItem(
    roomId,
    name,
    avatarUrl,
    lastMsg,
    lastMsgTime,
    unseenMsgNum,
    Room.TYPE_GROUP,
    lastSenderId,
    lastSenderName,
    lastTypingMember)