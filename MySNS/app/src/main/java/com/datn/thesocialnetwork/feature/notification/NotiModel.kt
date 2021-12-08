package com.datn.thesocialnetwork.feature.notification

data class NotiModel(
var user: String = "",
var body: String = "",
var title: String = "",
var send: String = "",
var notiType: String = "",
var icon: Int = 0
)

data class Sender(
    var data: NotiModel,
    var toUser: String = "",
)

data class Token(
    var token: String = ""
)
