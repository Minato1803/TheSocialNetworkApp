package com.datn.thesocialnetwork.feature.profile.viewmodel

import android.app.Application
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.Response
import com.datn.thesocialnetwork.core.api.status.FollowStatus
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.api.status.SearchFollowStatus
import com.datn.thesocialnetwork.core.util.FirebaseNode
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.core.util.SystemUtils.normalize
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.FollowRespository
import com.datn.thesocialnetwork.data.repository.UserRepository
import com.datn.thesocialnetwork.data.repository.model.FollowerModel
import com.datn.thesocialnetwork.data.repository.model.PostsModel
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class ProfileViewModel @Inject constructor(
    private val mApp: Application,
    private val respository: FollowRespository,
) : AndroidViewModel(mApp) {

    val livePost = MutableLiveData<PostsModel>()
    val getFollowingLiveData = MutableLiveData<Response<List<String>>>()
    val getFollowerLiveData = MutableLiveData<Response<List<String>>>()
    val followLiveData = MutableLiveData<Response<String>>()
    val unfollowLiveData = MutableLiveData<Response<String>>()

    private val _canDoFollowUnfollowOperation = MutableStateFlow(true)
    val canDoFollowUnfollowOperation = _canDoFollowUnfollowOperation.asStateFlow()
    private val _selectedUser: MutableStateFlow<UserDetail?> = MutableStateFlow(null)
    val selectedUser = _selectedUser.asStateFlow()
    private val _loggedUserFollowing = respository.loggedUserFollowing

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized = _isInitialized.asStateFlow()

    private val auth = Firebase.auth

    private val _loggedUser = MutableStateFlow(auth.currentUser)
    val loggedUser = _loggedUser.asStateFlow()

    private val _userFollowersFlow: MutableStateFlow<SearchFollowStatus> =
        MutableStateFlow(SearchFollowStatus.Sleep)
    val userFollowersFlow = _userFollowersFlow.asStateFlow()

    private val _userFollowingFlow: MutableStateFlow<SearchFollowStatus> =
        MutableStateFlow(SearchFollowStatus.Sleep)
    val userFollowingFlow = _userFollowingFlow.asStateFlow()

//    private val _loggedUserData = MutableStateFlow(UserResponse())
//    val loggedUserData = _loggedUserData.asStateFlow()

    fun updateFollowList() {
        respository.loadLoggedUserFollowing(GlobalValue.USER!!.uidUser)
    }
    private var _isSelectedUserFollowedByLoggedUserVal: IsUserFollowed = IsUserFollowed.UNKNOWN
    val isSelectedUserFollowedByLoggedUser: Flow<IsUserFollowed> = _selectedUser.combine(
        _loggedUserFollowing,
    ) { selected, following ->

        _isSelectedUserFollowedByLoggedUserVal = if (selected == null)
        {
            IsUserFollowed.UNKNOWN
        }
        else
        {
            if (following.contains(selected.uidUser)) IsUserFollowed.YES
            else IsUserFollowed.NO
        }
        _isSelectedUserFollowedByLoggedUserVal
    }

    @ExperimentalCoroutinesApi
    fun initUser(user: UserDetail)
    {
        _isInitialized.value = true
        _selectedUser.value = user

        viewModelScope.launch {
            respository.getUserFollowersFlow(user.uidUser).collectLatest {
                _userFollowersFlow.value = it
            }
        }

        viewModelScope.launch {
            respository.getUserFollowingFlow(user.uidUser).collectLatest {
                _userFollowingFlow.value = it
            }
        }

    }


    fun getFollowers(): Flow<GetStatus<List<String>>>
    {
        val id = _selectedUser.value?.uidUser
        return if (_isInitialized.value && id != null)
        {
            respository.getFollowers(id)
        }
        else
        {
            flow { }
        }
    }

    fun getFollowing(): Flow<GetStatus<List<String>>>
    {
        val id = _selectedUser.value?.uidUser
        return if (_isInitialized.value && id != null)
        {
            respository.getFollowing(id)
        }
        else
        {
            flow { }
        }
    }

//    fun getListFollower(userId : String) {
//        viewModelScope.launch(Dispatchers.IO) {
//
//            if (!SystemUtils.hasInternetConnection(mApp)) {
//
//                getFollowingLiveData.postValue(Response.Error(mApp.getString(R.string.no_internet)))
//            } else {
//
//                getFollowingLiveData.postValue(Response.Loading())
//                respository.getListFollower(userId)
//                    .addListenerForSingleValueEvent( object : ValueEventListener
//                    {
//                        override fun onDataChange(snapshot: DataSnapshot)
//                        {
//                            val followers = snapshot.getValue(FirebaseNode.followedType)
//                            getFollowingLiveData.postValue(Response.Success(
//                                followers?.map {
//                                    it.value.sourceId
//                                } ?: listOf()
//                            ))
//                        }
//
//                        override fun onCancelled(error: DatabaseError)
//                        {
//                            getFollowingLiveData.postValue(Response.Error(error.message))
//                        }
//                    })
//            }
//        }
//    }
//
//    fun getListFollowing(userId : String) {
//        viewModelScope.launch(Dispatchers.IO) {
//
//            if (!SystemUtils.hasInternetConnection(mApp)) {
//
//                getFollowerLiveData.postValue(Response.Error(mApp.getString(R.string.no_internet)))
//            } else {
//
//                getFollowerLiveData.postValue(Response.Loading())
//                respository.getListFollower(userId)
//                    .addListenerForSingleValueEvent( object : ValueEventListener
//                    {
//                        override fun onDataChange(snapshot: DataSnapshot)
//                        {
//                            val followers = snapshot.getValue(FirebaseNode.followedType)
//                            getFollowerLiveData.postValue(Response.Success(
//                                followers?.map {
//                                    it.value.desId
//                                } ?: listOf()
//                            ))
//                        }
//
//                        override fun onCancelled(error: DatabaseError)
//                        {
//                            getFollowerLiveData.postValue(Response.Error(error.message))
//                        }
//                    })
//            }
//        }
//    }

    /**
     * Check if given id is the same as logged user
     */
//    fun isOwnAccountId(userId: String): Boolean =
//        loggedUser.value?.uid == userId
//
//    fun isOwnAccountName(username: String): Boolean
//    {
//        return _loggedUserData.value.userDetail.userName == username.normalize()
//    }

    @ExperimentalCoroutinesApi
    fun followUnfollow()
    {
        Log.d("TAG", "${_canDoFollowUnfollowOperation.value.toString()} ${_isSelectedUserFollowedByLoggedUserVal.toString()}" )
        if (_canDoFollowUnfollowOperation.value)
        {
            when (_isSelectedUserFollowedByLoggedUserVal)
            {
                IsUserFollowed.UNKNOWN -> follow()
                IsUserFollowed.YES -> unfollow()
                IsUserFollowed.NO -> follow()
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun follow()
    {
        val userToFollow = _selectedUser.value
        Log.d("TAG","selectUser $userToFollow")
        if (userToFollow != null)
        {
            viewModelScope.launch {
                if (_canDoFollowUnfollowOperation.value)
                {
                    respository.follow(userToFollow.uidUser).collectLatest {
                        _canDoFollowUnfollowOperation.value = it != FollowStatus.LOADING
                    }
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun unfollow()
    {
        val userToUnfollow = _selectedUser.value
        if (userToUnfollow != null)
        {
            viewModelScope.launch {
                if (_canDoFollowUnfollowOperation.value)
                {
                    respository.unfollow(userToUnfollow.uidUser).collectLatest {
                        _canDoFollowUnfollowOperation.value = it != FollowStatus.LOADING
                    }
                }
            }
        }
    }

    enum class IsUserFollowed
    {
        UNKNOWN,
        YES,
        NO
    }
}