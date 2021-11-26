package com.datn.thesocialnetwork.data.repository.model

import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse

sealed class SearchModel() {
    data class UserItem(val user: UserModel?) : SearchModel()
    data class TagItem(val tag: TagModel) : SearchModel()
}
