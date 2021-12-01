package com.datn.thesocialnetwork.core.util

import androidx.fragment.app.Fragment
import com.datn.thesocialnetwork.data.repository.model.*
import com.datn.thesocialnetwork.data.repository.model.chat_room.Room
import com.datn.thesocialnetwork.data.repository.model.post.status.CommentModel
import com.google.firebase.database.GenericTypeIndicator

object Const {

    // Retrofit config.
    const val READ_TIMEOUT = 5_000L
    const val WRITE_TIMEOUT = 5_000L
    const val CONNECT_TIMEOUT = 10_000L

    // google SignIn config
    const val WEB_CLIENT_ID =
        "325160175322-ok0cgdcnnd5o2vtls8nkatk9pjsm63bs.apps.googleusercontent.com"

    const val avatarDefaultUrl =
        "https://firebasestorage.googleapis.com/v0/b/mysns-3ef38.appspot.com/o/MySNS%2Favatar%2Favatar_default.png?alt=media&token=c422d047-dba9-4069-aafe-94d67f44026c"

    const val REGEX_LETTER = "[a-zA-z]"
    const val REGEX_NUMBER = "[0-9]"
    const val REGEX_SPECIAL_CHAR = "[!@#\$%&*()_+=|<>?{}\\\\[\\\\]~-]"

    const val DATE_FORMAT = "dd-MM-yyyy"
    const val DATE_TIME_FORMAT_MESSAGE = "HH:mm dd/MM/yyyy"

    const val RECOMMENDED_COLUMNS = 2

    const val parentStorageFolder = "MySNS"

}

object FirebaseNode {
    const val user = "user"
    const val follow = "follow"
    const val tag = "hashTags"
    const val chat = "chat"
    const val post = "posts"

    //user
    const val uidUser = "uidUser"
    const val uidGoogle = "uidGoogle"
    const val userName = "userName"
    const val firstName = "firstName"
    const val lastName = "lastName"
    const val birthday = "birthday"
    const val gender = "gender"
    const val email = "email"
    const val password = "password"
    const val avatarUrl = "avatarUrl"
    const val phoneNumber = "phoneNumber"
    const val onlineStatus = "onlineStatus"
    const val description = "description"

    //follow
    const val sourceId = "sourceId"
    const val desId = "desId"
    val followedType = object : GenericTypeIndicator<HashMap<String, FollowerModel>?>()
    {}

    //message
    const val messageContent = "textContent"
    const val messageImgUrl = "imageUrl"
    const val messageTime = "time"
    const val messageSender = "sender"
    val messageType = object : GenericTypeIndicator<HashMap<String, ChatMessage>>()
    {}

    // chat
    const val messageName = "Messages"
    const val messageUser1 = "u1"
    const val messageUser2 = "u2"
    const val messageAllField = "msg"
    val conversationsType = object : GenericTypeIndicator<HashMap<String, Conversation>>()
    {}
    // room chat
    const val rooms = "Rooms"
    const val roomId = "roomId"
    const val createdTime = "createdTime"
    const val name = "name"
    val roomType = object : GenericTypeIndicator<HashMap<String, Room>>()
    {}
    // posts
    const val postsName = "Posts"
    const val postImageUrl = "image"
    const val postscontent = "content"
    const val postsOwner = "ownerId"
    const val postsCreatedTime = "createdTime"
    const val postsUpdatedTime = "updatedTime"
    const val reactCount = "reactCount"
    const val commentCount = "commentCount"
    const val shareCount = "shareCount"
    val postsType = object : GenericTypeIndicator<HashMap<String, PostsModel>>()
    {}
    // image posts
    const val imagePost = "imageUrl"
    const val imageCreated = "imageCreated"
    val imageType = object : GenericTypeIndicator<HashMap<String, PostsImage>>()
    {}
    //hashtag and mentions
    const val hashTag = "Hashtags"
    val hashtagsType = object : GenericTypeIndicator<HashMap<String, HashMap<String, Boolean>>>()
    {}
    const val mentions = "Mentions"
    // likes
    const val postLikes = "PostLikes"
    val postsLikes = object : GenericTypeIndicator<HashMap<String, Boolean>>()
    {}
    // comments
    const val comments = "Comments"
    const val commentContent = "content"
    const val commentTime = "time"
    const val commentOwner = "ownerId"
    val commentType = object : GenericTypeIndicator<HashMap<String, CommentModel>>()
    {}
    // Reports
    const val reportPost = "Reports"
    const val reportPostId = "postId"
    const val reporter = "reporter"
    const val reportMessage = "message"
    const val reportTime = "CreatedTime"

}

object KEY {
    const val USER_ID = "#userId"
    const val USER = "#user"
    const val LOGIN = "#login"
}

inline val Fragment.isFragmentAlive
    get() = !(this.isRemoving || this.activity == null || this.isDetached || !this.isAdded || this.view == null)