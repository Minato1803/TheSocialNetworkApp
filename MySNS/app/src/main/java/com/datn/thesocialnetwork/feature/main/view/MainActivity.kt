package com.datn.thesocialnetwork.feature.main.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.ModelMapping
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.model.ConversationItem
import com.datn.thesocialnetwork.databinding.ActivityMainBinding
import com.datn.thesocialnetwork.feature.chat.view.ChatFragment
import com.datn.thesocialnetwork.feature.chat.viewmodel.ChatViewModel
import com.datn.thesocialnetwork.feature.home.view.HomeFragment
import com.datn.thesocialnetwork.feature.login.view.LoginFragment
import com.datn.thesocialnetwork.feature.login.viewmodel.LoginViewModel
import com.datn.thesocialnetwork.feature.notification.Token
import com.datn.thesocialnetwork.feature.post.view.CreatePostFragment
import com.datn.thesocialnetwork.feature.profile.view.ProfileFragment
import com.datn.thesocialnetwork.feature.profile.viewmodel.ProfileViewModel
import com.datn.thesocialnetwork.feature.search.view.SearchFragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var bd: ActivityMainBinding
    private val mLoginViewModel: LoginViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    private val fragHome = HomeFragment()
    private val fragSearch = SearchFragment()
    private val fragChat = ChatFragment()
    private val fragProfile = ProfileFragment()
    private val fragLogin = LoginFragment()

    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    lateinit var badge_dashboard: BadgeDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bd = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bd.root)
        setupNavigation()
        setInit()
        setObserveData()
        setEvent()
    }

    private fun setupNavigation() {
        with(bd.bottomNavMain)
        {
            background = null // clear shadow
            menu.getItem(2).isEnabled = false // disable placeholder
        }
    }


    private fun setEvent() {
        bd.bottomNavMain.setOnNavigationItemSelectedListener { menuItem ->
            clickNavigateSubScreen(menuItem)
        }
        actionBarDrawerToggle.setToolbarNavigationClickListener {

        }
        bd.fabAdd.setOnClickListener {
            CreatePostFragment.newInstance()
                .show(supportFragmentManager, "CreatePostDialogFragment")
        }
    }

    private fun setObserveData() {
        mLoginViewModel.liveDataCheckLogin.observe(this, { data ->
            observeGetUser(data)
        })
    }

    private fun observeGetUser(data: UserResponse?) {
        if (data != null) {
            Log.d("TAG", "get user login $data")
            GlobalValue.USER = data
            GlobalValue.USER_DETAIL = ModelMapping.mapToUserModel(GlobalValue.USER!!)

            val userDetail = ModelMapping.createUserDetail(GlobalValue.USER!!.userDetail, 0)
            profileViewModel.updateOnlineStatus(data.uidUser, userDetail)
            GlobalValue.USER!!.userDetail.onlineStatus = 0
            GlobalValue.USER_DETAIL!!.onlineStatus = 0

            chatViewModel.updateConversations()
            setObserveChatList()
            updateToken(FirebaseInstanceId.getInstance().token)
        } else {
            SystemUtils.signOut(mGoogleSignInClient, this)
        }

        if (GlobalValue.USER == null) {
            setCurrentFragment(fragLogin)
        } else {
            setCurrentFragment(fragHome)
        }
    }

    private fun setInit() {
        if (!SystemUtils.hasInternetConnection(this)) {
            SystemUtils.showDialogNoInternetConnection(this)
        }
        mLoginViewModel.checkLogin()
        setSupportActionBar(bd.toolbar)
        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, bd.drawerLayout, bd.toolbar, R.string.open, R.string.close)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        actionBarDrawerToggle.syncState()

        badge_dashboard = bd.bottomNavMain.getOrCreateBadge(R.id.navChat)
        badge_dashboard.backgroundColor = Color.RED
        badge_dashboard.badgeTextColor = Color.BLACK
        badge_dashboard.maxCharacterCount = 9
        badge_dashboard.clearNumber()
        badge_dashboard.isVisible = false
    }

    private fun setObserveChatList() {
        lifecycleScope.launchWhenStarted {
            chatViewModel.conversation.collectLatest {
                when (it) {
                    is GetStatus.Success -> {
                        setbadgeCount(it.data)
                    }
                }
            }
        }
    }

    private fun setbadgeCount(data: List<ConversationItem>) {
        var count = 0
        data.forEach {
            if (it.lastMessage.isRead == "false" && it.lastMessage.sender != GlobalValue.USER!!.uidUser) {
                count++
            }
        }
        if (count > 0) {
            badge_dashboard.number = count
            badge_dashboard.isVisible = true
        } else {
            badge_dashboard.clearNumber()
            badge_dashboard.isVisible = false
        }
    }

    fun clickNavigateSubScreen(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.navHome -> {
                setCurrentFragment(fragHome)
                true
            }

            R.id.navSearch -> {
                setCurrentFragment(fragSearch)
                true
            }

            R.id.navChat -> {
                setCurrentFragment(fragChat)
                true
            }

            R.id.navProfile -> {
                setCurrentFragment(fragProfile)
                true
            }

            else -> false
        }
    }

    private fun setCurrentFragment(frag: Fragment) {

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragContainer, frag)
            .commit()
    }

    fun updateToken(token: String?) {
        val user = FirebaseAuth.getInstance().currentUser
        var mUID = ""
        if (user != null) {
            mUID = user.uid
        }
        val ref = FirebaseDatabase.getInstance().getReference("Tokens")
        val mToken = token?.let { Token(it) }
        ref.child(mUID).setValue(mToken)
    }

    fun enableLayoutBehaviour() {
        bd.bottomAppBar.isVisible = true
        bd.fabAdd.isVisible = true
        val paramContainer: CoordinatorLayout.LayoutParams =
            bd.host.layoutParams as CoordinatorLayout.LayoutParams
        paramContainer.behavior = AppBarLayout.ScrollingViewBehavior()

        val paramToolbar = bd.toolbar.layoutParams as AppBarLayout.LayoutParams
        paramToolbar.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
    }

    fun disableLayoutBehaviour() {
        val paramContainer: CoordinatorLayout.LayoutParams =
            bd.host.layoutParams as CoordinatorLayout.LayoutParams
        paramContainer.behavior = null

        val paramToolbar = bd.toolbar.layoutParams as AppBarLayout.LayoutParams
        paramToolbar.scrollFlags = 0
    }

    fun showSnackbar(
        message: String,
        buttonText: String? = null,
        action: () -> Unit = {},
        length: Int = Snackbar.LENGTH_LONG,
        gravity: Int = Gravity.TOP,
    ) = bd.host.showSnackbarGravity(message, buttonText, action, length, gravity)

    fun CoordinatorLayout.showSnackbarGravity(
        message: String,
        buttonText: String? = null,
        action: () -> Unit = {},
        length: Int = Snackbar.LENGTH_LONG,
        gravity: Int = Gravity.TOP,
    ) {
        val s = Snackbar
            .make(this, message, length)

        buttonText?.let {
            s.setAction(it) {
                action()
            }
        }

        val params = s.view.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = gravity
        s.view.layoutParams = params
        s.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE

        s.show()
    }
}