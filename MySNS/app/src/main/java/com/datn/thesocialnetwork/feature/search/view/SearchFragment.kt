package com.datn.thesocialnetwork.feature.search.view

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.SearchStatus
import com.datn.thesocialnetwork.core.util.Const
import com.datn.thesocialnetwork.core.util.SystemUtils.hideKeyboard
import com.datn.thesocialnetwork.core.util.ViewUtils.setActionBarTitle
import com.datn.thesocialnetwork.core.util.isFragmentAlive
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.model.TagModel
import com.datn.thesocialnetwork.databinding.FragmentSearchBinding
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.profile.view.ProfileFragment
import com.datn.thesocialnetwork.feature.search.viewmodel.SearchModelAdapter
import com.datn.thesocialnetwork.feature.search.viewmodel.SearchViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class SearchFragment : Fragment() {

    private var binding: FragmentSearchBinding? = null
    lateinit var mMainActivity: MainActivity
    private val mSearchViewModel : SearchViewModel by viewModels()

    private lateinit var menuItemSearch: MenuItem
    private lateinit var menuItemSearchType: MenuItem
    private lateinit var searchView: SearchView
    lateinit var actionBarDrawerToggle : ActionBarDrawerToggle

    private var areRecommendedPostsLoading = false
    private lateinit var recommendedLayoutManager: GridLayoutManager
    private lateinit var searchLayoutManager: LinearLayoutManager

    @Inject
    lateinit var searchModelAdapter: SearchModelAdapter
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(layoutInflater, container, false)
        setHasOptionsMenu(true)
        recommendedLayoutManager = GridLayoutManager(
            requireContext(),
            Const.RECOMMENDED_COLUMNS
        )
        searchLayoutManager = LinearLayoutManager(requireContext())

        setEvent()
        setInit()
        setObserveData()
        return binding!!.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search, menu)

        menuItemSearch = menu.findItem(R.id.itemSearch)
        menuItemSearchType = menu.findItem(R.id.itemSearchType)
        searchView = menuItemSearch.actionView as SearchView

        setupCollecting()

        mSearchViewModel.currentQuery?.let {
            menuItemSearch.expandActionView()
            searchView.setQuery(it, false)
            searchView.clearFocus()
            search(it)
        }

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener
            {
                override fun onQueryTextSubmit(textInput: String?): Boolean
                {
                    hideKeyboard(requireContext())
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean
                {
                    search(query)
                    return true
                }
            }
        )

        menuItemSearch.setOnActionExpandListener(
            object : MenuItem.OnActionExpandListener
            {
                override fun onMenuItemActionExpand(p0: MenuItem?): Boolean
                {
                    return true
                }

                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean
                {
                    return true
                }
            }
        )
    }

    private fun setupCollecting() {
        /**
         * Collect recommended posts
         */


        /**
         * Collect search type
         */
        lifecycleScope.launchWhenStarted {
            mSearchViewModel.currentSearchType.collectLatest {
                setSearchIcon(it)
                search(searchView.query.toString())
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun search(query: String) {
        //TODO: post logic
        if (isFragmentAlive)
        {
            if (query.isEmpty())
            {
                //todo: post rcv
            }
            else
            {
                binding!!.rvSearch.adapter = searchModelAdapter
                binding!!.rvSearch.layoutManager = searchLayoutManager
                binding!!.progressBarPosts.isVisible = false

                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    mSearchViewModel.search(query).collectLatest {
                        when (it)
                        {
                            is SearchStatus.Interrupted ->
                            {
                                binding!!.progressBarSearch.isVisible = false
                                displayEmptyResult(false)
                            }
                            SearchStatus.Loading ->
                            {
                                binding!!.progressBarSearch.isVisible = true
                                displayEmptyResult(false)
                            }
                            is SearchStatus.Success ->
                            {
                                Log.d("TAG","success ${it.result}")
                                binding!!.progressBarSearch.isVisible = false
                                searchModelAdapter.submitList(it.result)
                                displayEmptyResult(it.result.isEmpty())
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId)
        {
            R.id.itemSearchType ->
            {
                mSearchViewModel.selectNextSearchType()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setEvent() {

    }

    private fun setInit() {
        setMainView()
        setupAdapters()
        setupRecycler()
    }

    private fun setMainView() {
        // setting main view
        mMainActivity.bd.appBarLayout.isVisible = true
        mMainActivity.bd.bottomAppBar.isVisible = true
        mMainActivity.bd.fabAdd.isVisible = true
        actionBarDrawerToggle = ActionBarDrawerToggle(
            requireActivity(),
            mMainActivity.bd.drawerLayout,
            mMainActivity.bd.toolbar,
            R.string.open, R.string.close)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        actionBarDrawerToggle.syncState()
    }

    private fun setObserveData() {

    }

    private fun displayEmptyResult(show: Boolean)
    {
        binding!!.txtEmptyResult.isVisible = show
        binding!!.imgEmptyResult.isVisible = show
    }

    private fun setupAdapters()
    {
        searchModelAdapter.apply {
            userListener = ::selectUser
            tagListener = ::selectTag
        }

//        TODO: postAdapter
    }

    private fun selectUser(user: UserDetail)
    {
        if (Firebase.auth.currentUser?.uid == user.uidUser)
        {
            val profileFragment = ProfileFragment()
            navigateFragment(profileFragment, "profileFragment")
        }
        else
        {
            //todo: send data user
            val userFragment = UserFragment.newInstance(user)
            navigateFragment(userFragment, "userFragment")
        }
    }

    private fun selectTag(tag: TagModel)
    {
        //TODO: navigate tag
    }


    private fun setupRecycler()
    {
        binding?.let {
            with(it.rvSearch)
            {
                layoutManager = recommendedLayoutManager
//                TODO: adapter = postAdapter
            }
        }
    }

    private fun setSearchIcon(searchType: SearchType)
    {
        val d = ContextCompat.getDrawable(requireContext(), searchType.icon)
        if (d != null)
        {
            DrawableCompat.setTint(
                d,
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            menuItemSearchType.icon = d
        }

        menuItemSearchType.title = getString(searchType.title)

        setActionBarTitle(searchType.actionBarTitle)
    }

    private fun navigateFragment(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(id, fragment, "tag")
            .addToBackStack(null)
            .commit()
    }

    override fun onStop()
    {
        super.onStop()
        if (searchView.query.isEmpty())
        {
            mSearchViewModel.clearQuery()
        }
    }

    enum class SearchType(
        @DrawableRes val icon: Int,
        @StringRes val title: Int,
        @StringRes val actionBarTitle: Int,
    )
    {
        USER(
            R.drawable.ic_person_24,
            R.string.user,
            R.string.find_user,
        ),
        TAG(
            R.drawable.ic_tag_24,
            R.string.tag,
            R.string.search_by_tags,
        )
    }
}