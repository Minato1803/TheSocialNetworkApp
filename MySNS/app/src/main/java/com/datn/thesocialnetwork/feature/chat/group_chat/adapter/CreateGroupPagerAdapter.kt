package com.datn.thesocialnetwork.feature.chat.group_chat.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.datn.thesocialnetwork.feature.chat.group_chat.view.CreateGroupFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.feature.chat.group_chat.view.RecentContactsFragment
import com.datn.thesocialnetwork.feature.chat.group_chat.view.RecommendContactsFragment

@ExperimentalCoroutinesApi
class CreateGroupPagerAdapter @Inject constructor(
    private val createGroupFragment: CreateGroupFragment
) : FragmentStateAdapter(createGroupFragment) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> RecentContactsFragment()
            else -> RecommendContactsFragment()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

    fun getTabTitle(position: Int): CharSequence? {
        return when (position) {
            1 -> createGroupFragment.getString(R.string.label_recent)
            else -> createGroupFragment.getString(R.string.label_recommend)
        }
    }
}