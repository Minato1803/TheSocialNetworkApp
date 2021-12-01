package com.datn.thesocialnetwork.data.repository

import android.net.Uri
import android.util.Log
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.Event
import com.datn.thesocialnetwork.core.api.Message
import com.datn.thesocialnetwork.core.api.status.DataStatus
import com.datn.thesocialnetwork.core.api.status.EventMessageStatus
import com.datn.thesocialnetwork.core.api.status.FirebaseStatus
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.Const
import com.datn.thesocialnetwork.core.util.FirebaseNode
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.core.util.SystemUtils.normalize
import com.datn.thesocialnetwork.data.datasource.firebase.FirebaseListener
import com.datn.thesocialnetwork.data.repository.model.PostsImage
import com.datn.thesocialnetwork.data.repository.model.PostsModel
import com.datn.thesocialnetwork.data.repository.model.post.status.CommentModel
import com.datn.thesocialnetwork.data.repository.model.post.status.LikeStatus
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@ExperimentalCoroutinesApi
class PostRepository @Inject constructor(
    private val mFirebaseDb: FirebaseDatabase,
    private val mStorage: FirebaseStorage,
    private val followRespository: FollowRespository,
) {

    private fun getStorage() = mStorage.reference
    private fun getDatabasePost() = mFirebaseDb.getReference(FirebaseNode.post)
    private fun getDatabaseHashTag() = mFirebaseDb.getReference(FirebaseNode.hashTag)
    private fun getDatabaseMentions() = mFirebaseDb.getReference(FirebaseNode.mentions)
    private fun getDatabasePostLike() = mFirebaseDb.getReference(FirebaseNode.postLikes)
    private fun getDatabasePostComment() = mFirebaseDb.getReference(FirebaseNode.comments)
    private fun getDatabaseReport() = mFirebaseDb.getReference(FirebaseNode.reportPost)

    private fun getPostsStorageRef(userId: String) =
        getStorage().child(Const.parentStorageFolder).child(FirebaseNode.post).child(userId)
    private fun getPostLikes(postId: String) = getDatabasePostLike().child(postId)
    private fun getPostComment(postId: String) = getDatabasePostComment().child(postId)
    private fun getUserPost(userId: String) = getDatabasePost().orderByChild(FirebaseNode.postsOwner)
            .equalTo(userId)
    fun getPostById(postId: String) = getDatabasePost().child(postId)
    /** interact*/
    fun getPostUserWhichLikes(postId: String, userId: String) = getPostLikes(postId).child(userId)


    fun uploadPost(
        listUri: ArrayList<Uri>,
        desc: String,
        hashtags: List<String>,
        mentions: List<String>,
    ): Flow<FirebaseStatus> = channelFlow {

        send(FirebaseStatus.Loading)
        Log.d("listUriNeedUpload", "${listUri.size}")
        val postImagesSize = listUri.size
        if (GlobalValue.USER != null) {
            val urlList: ArrayList<String> = ArrayList()
            for (uri in listUri) {
                val currentTime = System.currentTimeMillis()
                val uploadPostRef = getPostsStorageRef(GlobalValue.USER!!.uidUser)
                    .child("$currentTime")
                val storageTask = uploadPostRef.putFile(uri)

                storageTask
                    .addOnSuccessListener {
                        uploadPostRef.downloadUrl
                            .addOnCompleteListener {
                                urlList.add(it.result.toString())
                                Log.d("TAG", "size: ${urlList.size} ${postImagesSize}")
                                if (urlList.size == postImagesSize) {
                                    Log.d("TAG", "start upload to RTD")
                                    val postId = getDatabasePost().push().key
                                        ?: "${GlobalValue.USER!!.uidUser}_${currentTime}"

                                    val post = hashMapOf(
                                        FirebaseNode.postscontent to desc,
                                        FirebaseNode.postsOwner to GlobalValue.USER!!.uidUser,
                                        FirebaseNode.postsCreatedTime to System.currentTimeMillis(),
                                        FirebaseNode.postsUpdatedTime to System.currentTimeMillis(),
                                        FirebaseNode.reactCount to 0,
                                        FirebaseNode.commentCount to 0,
                                        FirebaseNode.shareCount to 0
                                    )
                                    // save post
                                    getDatabasePost().child(postId).setValue(post)
                                        .addOnSuccessListener {
                                            /**
                                             * Saving hashtags
                                             */
                                            if (hashtags.isNotEmpty()) {

                                                hashtags.forEach { tag ->
                                                    val tagRef =
                                                        getDatabaseHashTag().child(tag.lowercase(
                                                            Locale.getDefault()))
                                                    val h = mapOf(
                                                        postId to true
                                                    )
                                                    tagRef.updateChildren(h)
                                                }
                                            } else {
                                                //no hashtag
                                            }
                                            /**
                                             * Saving mentions
                                             */
                                            if (mentions.isNotEmpty()) {
                                                mentions.forEach { mention ->
                                                    val mentionRef =
                                                        getDatabaseMentions().child(mention.lowercase(
                                                            Locale.getDefault()))

                                                    val h = mapOf(
                                                        postId to true
                                                    )
                                                    mentionRef.updateChildren(h)
                                                }
                                            } else {
                                                //no mentions
                                            }
                                            //save image post
                                            var countSuccesss = 0
                                            urlList.forEach { image ->
                                                val imagePostsRef = getDatabasePost().child(postId)
                                                    .child(FirebaseNode.postImageUrl)
                                                val imageKey = imagePostsRef.push().key
                                                val imageItem = PostsImage(image)
                                                Log.d("UpImageDB", "$imageKey $imageItem")
                                                if (imageKey != null) {
                                                    imagePostsRef.child(imageKey).setValue(imageItem.toHashMap)
                                                        .addOnFailureListener {
                                                            launch {
                                                                send(FirebaseStatus.Failed(Message(R.string.image_post_was_not_uploaded)))
                                                                close()
                                                            }
                                                        }
                                                        .addOnSuccessListener {
                                                            countSuccesss++
                                                            Log.d("TAG",
                                                                "successCount $countSuccesss ${urlList.size}")
                                                            if (countSuccesss == urlList.size) {
                                                                launch {
                                                                    send(FirebaseStatus.Success(
                                                                        Message(R.string.image_post_was_uploaded)))
                                                                    close()
                                                                }
                                                            }
                                                        }
                                                } else {
                                                    // error
                                                    launch {
                                                        send(FirebaseStatus.Failed(Message(R.string.image_post_was_not_uploaded)))
                                                        close()
                                                    }
                                                }
                                            }

                                        }
                                        .addOnFailureListener { exception ->
                                            launch {
                                                send(
                                                    FirebaseStatus.Failed(
                                                        Message(
                                                            R.string.post_was_not_uploaded,
                                                            listOf(exception.localizedMessage ?: "")
                                                        )
                                                    )
                                                )
                                                close()
                                            }
                                        }
                                }
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("TAG", "upload storage fail: ${exception.toString()}")
                        launch {
                            send(
                                FirebaseStatus.Failed(
                                    Message(
                                        R.string.post_was_not_uploaded,
                                        listOf(exception.localizedMessage ?: "")
                                    )
                                )
                            )
                            close()
                        }
                    }

            }

        } else {
            send(FirebaseStatus.Failed(Message(R.string.no_logged_user)))
            close()
        }
        awaitClose()
    }

    /**Load all posts from followers*/
    @ExperimentalCoroutinesApi
    fun getPostsFromFollowers(userId: String) = channelFlow<GetStatus<List<PostWithId>>> {

        send(GetStatus.Loading)

        /**
         * first people that user follows has to be loaded
         */
        followRespository.getListFollowing(userId).addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(dataSnapshot: DataSnapshot)
                {
                    val followers = dataSnapshot.getValue(FirebaseNode.followedType)

                    if (followers != null)
                    {
                        val followingUsers = followers.map {
                            it.value.desId
                        }


                        var counter = 0
                        val allPosts = mutableListOf<PostWithId>()

                        fun checkIfAllQueriesWereMade()
                        {
                            synchronized(counter)
                            {
                                counter++

                                if (counter == followingUsers.size)
                                {
                                    launch {
                                        allPosts.sortByDescending { it.second.updatedTime }
                                        send(GetStatus.Success(allPosts))
                                        close()
                                    }
                                }
                            }
                        }

                        /**
                         * For every user that is observed
                         * posts are loaded
                         */
                        followingUsers.forEach { userId ->

                            getUserPost(userId).addListenerForSingleValueEvent(
                                object : ValueEventListener
                                {
                                    override fun onDataChange(snapshot: DataSnapshot)
                                    {

                                        val posts = snapshot.getValue(FirebaseNode.postsType)
                                        Log.d("postsSnapshot", "$posts")
                                        posts?.forEach { post ->
                                            Log.d("TAG","${post.key} ${post.value}")
                                            val images = post.value.image
                                            images?.forEach { image ->
                                                image.value.id = image.key
                                            }
                                            val postItem = PostWithId(post.key, post.value, images?.toList())
                                            Log.d("postItemFollower", "$postItem")
                                            allPosts.add(postItem)
                                        }

                                        checkIfAllQueriesWereMade()
                                    }

                                    override fun onCancelled(error: DatabaseError)
                                    {
                                        checkIfAllQueriesWereMade()
                                    }
                                }
                            )

                        }
                        // endregion

                    }
                    else // user doesn't follow anyone
                    {
                        launch {
                            send(GetStatus.Success(listOf()))
                            close()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError)
                {
                    launch {
                        send(GetStatus.Failed(Message(R.string.followers_not_loaded)))
                        close()
                    }
                }
            }
        )
        awaitClose()
    }

    @ExperimentalCoroutinesApi
    fun getUserPostsFlow(userId: String): Flow<GetStatus<List<PostWithId>>> = channelFlow {
        Log.d("TAG","start get own post")
        send(GetStatus.Loading)

        getUserPost(userId).addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val posts = snapshot.getValue(FirebaseNode.postsType)

                    if (posts != null)
                    {
                        val listPost = mutableListOf<PostWithId>()
                        posts.forEach{ post ->
                            val images = post.value.image
                            images?.forEach { image ->
                                image.value.id = image.key
                            }
                            if (images != null) {
                                val postItem = PostWithId(post.key, post.value, images.toList())
                                Log.d("postItemOwn", "${postItem.toString()}")
                                listPost.add(postItem)
                            }
                        }
                        launch {
                            send(GetStatus.Success(listPost))
                            close()
                        }
                    }
                    else
                    {

                        launch {
                            send(GetStatus.Success(listOf<PostWithId>()))
                            close()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    // cancel
                }
            }
        )

        awaitClose()
    }

    @ExperimentalCoroutinesApi
    fun getMentionedPosts(username: String): Flow<GetStatus<List<PostWithId>>> = channelFlow {
        send(GetStatus.Loading)

        getDatabaseMentions().child(username).addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {

                    val mentionIds: List<String> = snapshot.children.mapNotNull {
                        it.key
                    }
                    if (mentionIds.isEmpty())
                    {
                        launch {
                            send(GetStatus.Success(listOf<PostWithId>()))
                            close()
                        }

                    }
                    val postToDisplay = mentionIds.size
                    var postQueried = 0
                    val posts: MutableList<PostWithId> = mutableListOf()


                    fun sendDataAndCheckClose()
                    {
                        postQueried++

                        launch {
                            if (postQueried == postToDisplay)
                            {
                                send(GetStatus.Success(posts))
                                close()
                            }
                        }
                    }

                    mentionIds.forEach { id ->

                        getPostById(id).addListenerForSingleValueEvent(
                            object : ValueEventListener
                            {
                                override fun onDataChange(snapshot: DataSnapshot)
                                {
                                    val post = snapshot.getValue(PostsModel::class.java)
                                    if (post != null)
                                    {
                                        val postItem = PostWithId(id,post,post.image?.toList())
                                        Log.d("postItemMentions", "$id $post ${post.image?.toList()}")
                                        posts.add(postItem)
                                    }
                                    else
                                    {
                                        //wrong
                                    }
                                    sendDataAndCheckClose()
                                }

                                override fun onCancelled(error: DatabaseError)
                                {
                                    sendDataAndCheckClose()
                                }
                            }
                        )
                    }

                }

                override fun onCancelled(error: DatabaseError)
                {
                    close()
                }
            }
        )

        awaitClose()
    }

    @ExperimentalCoroutinesApi
    fun getLikedPostByUserId(userId: String): Flow<GetStatus<List<PostWithId>>> = channelFlow {
        send(GetStatus.Loading)

        getDatabasePostLike().orderByChild(userId).equalTo(true).addListenerForSingleValueEvent(

            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {

                    val likedIds: List<String> = snapshot.children.mapNotNull {
                        it.key
                    }

                    if (likedIds.isEmpty())
                    {
                        launch {
                            send(GetStatus.Success(listOf<PostWithId>()))
                            close()
                        }

                    }

                    val postToDisplay = likedIds.size
                    var postQueried = 0
                    val posts: MutableList<PostWithId> = mutableListOf()
                    fun sendDataAndCheckClose()
                    {
                        postQueried++

                        launch {
                            if (postQueried == postToDisplay)
                            {
                                send(GetStatus.Success(posts))
                                close()
                            }
                        }
                    }
                    likedIds.forEach { id ->

                        getPostById(id).addListenerForSingleValueEvent(
                            object : ValueEventListener
                            {
                                override fun onDataChange(snapshot: DataSnapshot)
                                {
                                    val post = snapshot.getValue(PostsModel::class.java)
                                    if (post != null)
                                    {
                                        val postItem = PostWithId(id,post,post.image?.toList())
                                        Log.d("postItemLiked", "$id $post ${post.image?.toList()}")
                                        posts.add(postItem)
                                    }
                                    else
                                    {
                                        //wrong
                                    }
                                    sendDataAndCheckClose()
                                }

                                override fun onCancelled(error: DatabaseError)
                                {
                                    sendDataAndCheckClose()
                                }
                            }
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    close()
                }
            }
        )

        awaitClose()
    }

    /** Detail post*/
    @ExperimentalCoroutinesApi
    fun getPost(postId: String): Flow<GetStatus<PostWithId>> = channelFlow {
        send(GetStatus.Loading)

        getPostById(postId).addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val post = snapshot.getValue(PostsModel::class.java)

                    if (post != null)
                    {
                        launch {
                            val postItem = PostWithId(postId,post,post.image?.toList())
                            Log.d("postItemGet", "$postId $post ${post.image?.toList()}")
                            send(GetStatus.Success(postItem))
                            close()
                        }
                    }
                    else
                    {
                        launch {
                            send(GetStatus.Failed(Message(R.string.something_went_wrong)))
                            close()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    launch {
                        send(GetStatus.Failed(Message(R.string.something_went_wrong)))
                        close()
                    }
                }
            }
        )

        awaitClose()
    }

    private val likeListeners: HashMap<Int, FirebaseListener<GetStatus<LikeStatus>>> = hashMapOf()

    fun removeLikeListener(ownerHash: Int) {
        likeListeners[ownerHash]?.removeListener()
        likeListeners.remove(ownerHash)
    }

    fun getPostLikes(
        ownerHash: Int,
        postId: String,
    ): Flow<GetStatus<LikeStatus>> {

        return channelFlow {
            send(GetStatus.Loading)

            val dr = getPostLikes(postId)

            val l = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    launch {

                        val v = GetStatus.Success(
                            LikeStatus(
                                isPostLikeByLoggedUser = snapshot.child(GlobalValue.USER!!.uidUser)
                                    .exists(),
                                likeCounter = snapshot.childrenCount
                            )
                        )

                        send(v)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    launch {
                        val v =
                            GetStatus.Failed(Message(R.string.str_something_went_wrong))
                        send(v)
                    }
                }
            }

            likeListeners[ownerHash] = FirebaseListener(l, dr)
            likeListeners[ownerHash]?.addListener()

            awaitClose()
        }
    }

    private val commentCounterListeners: HashMap<Int, FirebaseListener<GetStatus<Long>>> =
        hashMapOf()

    fun removeCommentCounterListener(ownerHash: Int) {
        commentCounterListeners[ownerHash]?.removeListener()
        commentCounterListeners.remove(ownerHash)
    }

    fun getCommentsCounter(
        ownerHash: Int,
        postId: String,
    ): Flow<GetStatus<Long>> = channelFlow {

        send(GetStatus.Loading)

        val dr = getPostComment(postId)

        val l = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                launch {
                    val v = GetStatus.Success(snapshot.childrenCount)
                    send(v)
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
                launch {
                    val v = GetStatus.Failed(Message(R.string.str_something_went_wrong))
                    send(v)
                }
            }

        }

        commentCounterListeners[ownerHash] = FirebaseListener(l, dr)
        commentCounterListeners[ownerHash]?.addListener()

        awaitClose()
    }

    fun getUsersWhoLikePost(postId: String): Flow<GetStatus<List<String>>> = channelFlow {
        send(GetStatus.Loading)
        Log.d("TAG", "start get users who like this post")
        getPostLikes(postId).addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val like = snapshot.getValue(FirebaseNode.postsLikes)
                    Log.d("usersLike", like.toString())
                    launch {
                        send(
                            GetStatus.Success(
                                like?.keys?.toList() ?: listOf()
                            )
                        )
                        close()
                    }
                }

                override fun onCancelled(error: DatabaseError)
                {
                    launch {
                        send(GetStatus.Failed(Message(R.string.something_went_wrong)))
                        close()
                    }
                }

            }
        )

        awaitClose()
    }

    fun likeDislikePost(postId: String, like: Boolean)
    {
        if (like)
        {
            Log.d("interact", "Like")
            getPostUserWhichLikes(postId, GlobalValue.USER!!.uidUser).setValue(true)
        }
        else
        {
            Log.d("interact","Dislike")
            getPostUserWhichLikes(postId, GlobalValue.USER!!.uidUser).removeValue()
        }
    }

    fun editPost(
        postId: String,
        newDesc: String,
        newHashtags: List<String>,
        newMentions: List<String>,
        oldHashtags: List<String>,
        oldMentions: List<String>,
    ): Flow<EventMessageStatus> = channelFlow {
        send(EventMessageStatus.Loading)

        /**
         * Remove hashtags
         */
        oldHashtags.forEach { old ->
            if (!newHashtags.contains(old))
            {
               getDatabaseHashTag().child(old).child(postId).removeValue()
            }
        }
        /**
         * Add new hashtags
         */
        newHashtags.forEach { new ->
            if (!oldHashtags.contains(new))
            {
                val tagRef = getDatabaseHashTag().child(new.normalize())
                val h = mapOf(
                    postId to true
                )
                tagRef.updateChildren(h)
            }
        }
        /**
         * Remove mentions
         */
        oldMentions.forEach { old ->
            if (!newMentions.contains(old))
            {
                getDatabaseMentions().child(old).child(postId).removeValue()
            }
        }
        /**
         * Add new mentions
         */
        newMentions.forEach { new ->
            if (!oldMentions.contains(new))
            {
                val mRef = getDatabaseMentions().child(new.normalize())
                val h = mapOf(
                    postId to true
                )
                mRef.updateChildren(h)
            }
        }
        getDatabasePost().child(postId)
            .child(FirebaseNode.postsUpdatedTime)
            .setValue(System.currentTimeMillis())
        getDatabasePost().child(postId)
            .child(FirebaseNode.postscontent)
            .setValue(newDesc)
            .addOnSuccessListener {
                launch {
                    send(EventMessageStatus.Success(Event(Message(R.string.post_edited))))
                }
            }
            .addOnFailureListener {
                launch {
                    send(EventMessageStatus.Failed(Event(Message(R.string.post_not_edited))))
                }
            }
        awaitClose()
    }

    fun reportPost(postId: String, reportMessage: String)
    {

        val key = postId + GlobalValue.USER!!.uidUser

        val report = hashMapOf(
            FirebaseNode.reportPostId to postId,
            FirebaseNode.reporter to GlobalValue.USER!!.uidUser,
            FirebaseNode.reportMessage to reportMessage,
            FirebaseNode.reportTime to System.currentTimeMillis(),
        )

        getDatabaseReport().child(key).setValue(report)

    }

    private var commentRef: DatabaseReference? = null
    private var commentListener: ValueEventListener? = null

    fun addComment(
        postId: String,
        comment: String
    ): Flow<FirebaseStatus> = channelFlow {

        send(FirebaseStatus.Loading)

        if (comment.isBlank())
        {
            send(FirebaseStatus.Failed(Message(R.string.comment_cannot_be_empty)))
            close()
        }
        else
        {
            val commentDb = getPostComment(postId)
            val commentKey = commentDb.push().key

            if (commentKey != null)
            {
                val body = hashMapOf<String, Any>(
                    FirebaseNode.commentOwner to GlobalValue.USER!!.uidUser,
                    FirebaseNode.commentContent to comment,
                    FirebaseNode.commentTime to System.currentTimeMillis()
                )

                commentDb.child(commentKey).setValue(body)
                    .addOnSuccessListener {
                        launch {
                            send(FirebaseStatus.Success(Message(R.string.comment_posted)))
                            close()
                        }
                    }
                    .addOnFailureListener {
                        launch {
                            send(FirebaseStatus.Failed(Message(R.string.something_went_wrong)))
                            close()
                        }
                    }
            }
            else
            {
                send(FirebaseStatus.Failed(Message(R.string.something_went_wrong)))
                close()
            }
        }

        awaitClose()
    }

    fun getComments(postId: String): Flow<DataStatus<CommentModel>> = channelFlow {
        send(DataStatus.Loading)

        /**
         * remove old listener
         */
        commentListener?.let {
            commentRef?.removeEventListener(it)
        }

        commentRef = getPostComment(postId)

        commentListener = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val comments = snapshot.getValue(FirebaseNode.commentType)
                if (comments != null)
                {
                    launch {
                        send(DataStatus.Success(comments))
                    }
                }
                else // no comments yet
                {
                    launch {
                        send(DataStatus.Success<CommentModel>(hashMapOf()))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError)
            {

            }
        }

        commentRef!!.addValueEventListener(commentListener!!)

        awaitClose()
    }

    fun removeCommentListener()
    {
        commentListener?.let {
            commentRef?.removeEventListener(it)
        }
    }
}