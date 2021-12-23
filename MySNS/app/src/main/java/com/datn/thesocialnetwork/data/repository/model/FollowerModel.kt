package com.datn.thesocialnetwork.data.repository.model

import java.io.Serializable

data class FollowerModel(
    var sourceId : String = "", // follower
    var desId : String = "", // user target
    var createdAt : String = "",
) : Serializable
