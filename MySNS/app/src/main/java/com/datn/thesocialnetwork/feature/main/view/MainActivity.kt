package com.datn.thesocialnetwork.feature.main.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.databinding.ActivityMainBinding
import com.datn.thesocialnetwork.feature.chat.view.ChatFragment
import com.datn.thesocialnetwork.feature.friends.view.FriendsFragment
import com.datn.thesocialnetwork.feature.home.view.HomeFragment
import com.datn.thesocialnetwork.feature.profile.view.ProfileFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var bd: ActivityMainBinding

    private val fragHome = HomeFragment()
    private val fragFriends = FriendsFragment()
    private val fragChat = ChatFragment()
    private val fragProfile = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bd = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bd.root)

        setInit()
        setObserveData()
        setEvent()
    }

    private fun setEvent() {
        bd.bottomNavMain.setOnNavigationItemSelectedListener { menuItem ->
            clickNavigateSubScreen(menuItem)
        }
    }

    private fun setObserveData() {
        //TODO("Not yet implemented")
    }

    private fun setInit() {
        if (!SystemUtils.hasInternetConnection(this)) {
            SystemUtils.showDialogNoInternetConnection(this)
        }
        setSupportActionBar(bd.toolbar)
        setCurrentFragment(fragHome)
    }

    private fun clickNavigateSubScreen(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.navHome -> {
                setCurrentFragment(fragHome)
                true
            }

            R.id.navFriends -> {
                setCurrentFragment(fragFriends)
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
}