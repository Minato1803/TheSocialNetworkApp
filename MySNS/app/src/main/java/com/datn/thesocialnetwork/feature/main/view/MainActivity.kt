package com.datn.thesocialnetwork.feature.main.view

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.databinding.ActivityMainBinding
import com.datn.thesocialnetwork.feature.chat.view.ChatFragment
import com.datn.thesocialnetwork.feature.search.view.SearchFragment
import com.datn.thesocialnetwork.feature.home.view.HomeFragment
import com.datn.thesocialnetwork.feature.login.view.LoginFragment
import com.datn.thesocialnetwork.feature.login.viewmodel.LoginViewModel
import com.datn.thesocialnetwork.feature.profile.view.ProfileFragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var bd: ActivityMainBinding
    private val mLoginViewModel: LoginViewModel by viewModels()

    private val fragHome = HomeFragment()
    private val fragSearch = SearchFragment()
    private val fragChat = ChatFragment()
    private val fragProfile = ProfileFragment()
    private val fragLogin = LoginFragment()

    lateinit var actionBarDrawerToggle : ActionBarDrawerToggle

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
    }

    private fun setObserveData() {
        mLoginViewModel.liveDataCheckLogin.observe(this, { data ->
            observeGetUserByPhoneNumber(data)
        })
    }

    private fun observeGetUserByPhoneNumber(data: UserResponse?) {
        if (data != null) {
            GlobalValue.USER = data
        } else {
            SystemUtils.signOut(mGoogleSignInClient, this)
        }

        if(GlobalValue.USER == null) {
            setCurrentFragment(fragLogin)
        }
        else {
            setCurrentFragment(fragHome)
        }
    }

    private fun setInit() {
        if (!SystemUtils.hasInternetConnection(this)) {
            SystemUtils.showDialogNoInternetConnection(this)
        }
        mLoginViewModel.checkLogin()
        setSupportActionBar(bd.toolbar)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, bd.drawerLayout, bd.toolbar, R.string.open, R.string.close)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        actionBarDrawerToggle.syncState()
    }

    private fun clickNavigateSubScreen(menuItem: MenuItem): Boolean {
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
    fun enableLayoutBehaviour()
    {
        bd.bottomAppBar.isVisible = true
        bd.fabAdd.isVisible = true
        val paramContainer: CoordinatorLayout.LayoutParams = bd.host.layoutParams as CoordinatorLayout.LayoutParams
        paramContainer.behavior = AppBarLayout.ScrollingViewBehavior()

        val paramToolbar = bd.toolbar.layoutParams as AppBarLayout.LayoutParams
        paramToolbar.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
    }

    fun disableLayoutBehaviour()
    {
        val paramContainer: CoordinatorLayout.LayoutParams = bd.host.layoutParams as CoordinatorLayout.LayoutParams
        paramContainer.behavior = null

        val paramToolbar = bd.toolbar.layoutParams as AppBarLayout.LayoutParams
        paramToolbar.scrollFlags = 0
    }
}