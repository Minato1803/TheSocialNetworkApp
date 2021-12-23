package com.datn.thesocialnetwork.feature.profile.viewmodel

import android.app.Application
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.Response
import com.datn.thesocialnetwork.core.api.status.FollowStatus
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.api.status.SearchFollowStatus
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.ModelMapping
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.FirebaseRepository
import com.datn.thesocialnetwork.data.repository.FollowRespository
import com.datn.thesocialnetwork.data.repository.PostRepository
import com.datn.thesocialnetwork.data.repository.UserRepository
import com.datn.thesocialnetwork.data.repository.model.PostsModel
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.datn.thesocialnetwork.feature.post.viewmodel.ViewModelPost
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
@ExperimentalCoroutinesApi
class ProfileViewModel @Inject constructor(
    private val mApp: Application,
    private val followRespository: FollowRespository,
    private val firebaseRespository: FirebaseRepository,
    private val userRespository: UserRepository,
    private val postRepository: PostRepository,
) : ViewModelPost(firebaseRespository, postRepository) {

    val livePost = MutableLiveData<PostsModel>()
    val liveUserModel = MutableLiveData<UserModel>()
    val getFollowerLiveData = MutableLiveData<Response<List<String>>>()
    val unfollowLiveData = MutableLiveData<Response<String>>()

    private val _userNotFound = MutableStateFlow(false)
    val userNotFound = _userNotFound.asStateFlow()

    private val _canDoFollowUnfollowOperation = MutableStateFlow(true)
    val canDoFollowUnfollowOperation = _canDoFollowUnfollowOperation.asStateFlow()

    private val _selectedUser: MutableStateFlow<UserModel?> = MutableStateFlow(null)
    val selectedUser = _selectedUser.asStateFlow()

    private val _loggedUserFollowing = followRespository.loggedUserFollowing

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized = _isInitialized.asStateFlow()

    private var _isSelectedUserFollowedByLoggedUserVal: IsUserFollowed = IsUserFollowed.UNKNOWN
    val isSelectedUserFollowedByLoggedUser: Flow<IsUserFollowed> = _selectedUser.combine(
        _loggedUserFollowing,
    ) { selected, following ->

        _isSelectedUserFollowedByLoggedUserVal = if (selected == null) {
            IsUserFollowed.UNKNOWN
        } else {
            if (following.contains(selected.uidUser))
                IsUserFollowed.YES
            else
                IsUserFollowed.NO
        }
        _isSelectedUserFollowedByLoggedUserVal
    }

    private val _userFollowersFlow: MutableStateFlow<SearchFollowStatus> =
        MutableStateFlow(SearchFollowStatus.Sleep)
    val userFollowersFlow = _userFollowersFlow.asStateFlow()

    private val _userFollowingFlow: MutableStateFlow<SearchFollowStatus> =
        MutableStateFlow(SearchFollowStatus.Sleep)
    val userFollowingFlow = _userFollowingFlow.asStateFlow()

    private val _uploadedPosts: MutableStateFlow<GetStatus<List<PostWithId>>> = MutableStateFlow(
        GetStatus.Sleep
    )
    val uploadedPosts = _uploadedPosts.asStateFlow()

    private val _category: MutableStateFlow<DisplayPostCategory> = MutableStateFlow(
        DisplayPostCategory.UPLOADED
    )
    val category = _category.asStateFlow()

    private var _mentionPosts: MutableStateFlow<GetStatus<List<PostWithId>>> = MutableStateFlow(
        GetStatus.Sleep
    )
    val mentionPosts = _mentionPosts.asStateFlow()

    private var _likedPosts: MutableStateFlow<GetStatus<List<PostWithId>>> = MutableStateFlow(
        GetStatus.Sleep
    )
    val likedPosts = _likedPosts.asStateFlow()

    private var _markedPosts: MutableStateFlow<GetStatus<List<PostWithId>>> = MutableStateFlow(
        GetStatus.Sleep
    )
    val markedPosts = _markedPosts.asStateFlow()

    @ExperimentalCoroutinesApi
    fun initUser(user: UserModel) {
        _isInitialized.value = true
        _selectedUser.value = user
        Log.d("selectedUser", "${user.uidUser} ${user.userName}")
        loadDataInitUser(user)
    }

    fun loadDataInitUser(user: UserModel) {
        viewModelScope.launch{
            followRespository.getUserFollowersFlow(user.uidUser).collectLatest {
                _userFollowersFlow.value = it
            }
        }

        viewModelScope.launch {
            followRespository.getUserFollowingFlow(user.uidUser).collectLatest {
                _userFollowingFlow.value = it
            }
        }

        viewModelScope.launch{
            Log.d("TAG", "get own post")
            postRepository.getUserPostsFlow(user.uidUser).collectLatest {
                _uploadedPosts.value = it
            }
        }

        viewModelScope.launch {
            postRepository.getMentionedPosts(user.userName.lowercase()).collectLatest {
                _mentionPosts.value = it
            }
        }

        viewModelScope.launch {
            postRepository.getLikedPostByUserId(user.uidUser).collectLatest {
                _likedPosts.value = it
            }
        }

        viewModelScope.launch{
            postRepository.getMarkedPostByUserId(user.uidUser).collectLatest {
               _markedPosts.value = it
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun initWithLoggedUser() = initWithUserId(firebaseRespository.requireUser.uid)

    @ExperimentalCoroutinesApi
    fun initWithUserId(userId: String) {
        _isInitialized.value = true
        Log.d("TAG", "start get user from db")
        userRespository.getUserByID(userId).child(userId)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val users = snapshot.getValue(UserDetail::class.java)
                        Log.d("getUserValue", "${users.toString()}")
                        if (users != null) {
                            val userResponse = UserResponse(userId, users)
                            Log.d("getUserFromDB",
                                "${userResponse.uidUser} ${userResponse.userDetail}")

                            initUser(ModelMapping.mapToUserModel(userResponse))
                            liveUserModel.postValue(ModelMapping.mapToUserModel(userResponse))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //error
                    }
                }
            )
    }

    @ExperimentalCoroutinesApi
    fun initWithUsername(username: String) {
        _isInitialized.value = true

        userRespository.getUserByName(username)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val users = snapshot.children.toList()
                        Log.d("getUserValue", "${users.toString()}")
                        if (users.size == 1) {
                            val user = users[0].getValue(UserResponse::class.java)
                            Log.d("getUserValue", "${user.toString()}")
                            if (user != null) {
                                initUser(ModelMapping.mapToUserModel(user))
                                liveUserModel.postValue(ModelMapping.mapToUserModel(user))
                            }
                        } else {
                            //not found or found many
                            _userNotFound.value = true
                            Log.d("TAG", "many user has name")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //cancel
                    }
                }
            )
    }

    fun refreshUser() {
        Log.d("TAG", "refresh user ${_selectedUser.value?.uidUser.toString()}")
        _selectedUser.value?.uidUser?.let { id ->
            userRespository.getUserByID(id).child(id)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val users = snapshot.getValue(UserDetail::class.java)
                            Log.d("getUserValue", "${users.toString()}")
                            if (users != null) {
                                val userResponse = UserResponse(id, users)
                                Log.d("getUserFromDB",
                                    "${userResponse.uidUser} ${userResponse.userDetail}")

                                _selectedUser.value = ModelMapping.mapToUserModel(userResponse)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            //cancel
                        }
                    }
                )
        }
    }

    fun updateFollowList() {
        followRespository.loadLoggedUserFollowing(GlobalValue.USER!!.uidUser)
    }


    fun getFollowers(): Flow<GetStatus<List<String>>> {
        val id = _selectedUser.value?.uidUser
        return if (_isInitialized.value && id != null) {
            followRespository.getFollowers(id)
        } else {
            flow { }
        }
    }

    fun getFollowing(): Flow<GetStatus<List<String>>> {
        val id = _selectedUser.value?.uidUser
        return if (_isInitialized.value && id != null) {
            followRespository.getFollowing(id)
        } else {
            flow { }
        }
    }

    @ExperimentalCoroutinesApi
    fun followUnfollow() {
        Log.d("TAG",
            "${_canDoFollowUnfollowOperation.value.toString()} ${_isSelectedUserFollowedByLoggedUserVal.toString()}")
        if (_canDoFollowUnfollowOperation.value) {
            when (_isSelectedUserFollowedByLoggedUserVal) {
                IsUserFollowed.UNKNOWN -> follow()
                IsUserFollowed.YES -> unfollow()
                IsUserFollowed.NO -> follow()
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun follow() {
        val userToFollow = _selectedUser.value
        Log.d("TAG", "selectUser $userToFollow")
        if (userToFollow != null) {
            viewModelScope.launch {
                if (_canDoFollowUnfollowOperation.value) {
                    followRespository.follow(userToFollow.uidUser).collectLatest {
                        _canDoFollowUnfollowOperation.value = it != FollowStatus.LOADING
                    }
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun unfollow() {
        val userToUnfollow = _selectedUser.value
        if (userToUnfollow != null) {
            viewModelScope.launch {
                if (_canDoFollowUnfollowOperation.value) {
                    followRespository.unfollow(userToUnfollow.uidUser).collectLatest {
                        _canDoFollowUnfollowOperation.value = it != FollowStatus.LOADING
                    }
                }
            }
        }
    }

    private fun removeListeners() {
        followRespository.removeFollowingListener()
        followRespository.removeFollowersListener()
    }

    fun updateOnlineStatus(userId: String, userDetail: UserDetail) =
        viewModelScope.launch(Dispatchers.Main) {
            userRespository.updateUser(uidUser = userId, userDetail = userDetail)
            Log.d("Tag","success update")
        }

    enum class IsUserFollowed {
        UNKNOWN,
        YES,
        NO
    }

    enum class DisplayPostCategory(
        @StringRes val categoryName: Int,
    ) {
        UPLOADED(R.string.posts),
        MENTIONS(R.string.mentions),
        MARKED(R.string.marked)
    }
}

