package com.datn.thesocialnetwork.feature.search.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.datn.thesocialnetwork.core.api.status.SearchStatus
import com.datn.thesocialnetwork.data.repository.SearchRespository
import com.datn.thesocialnetwork.feature.search.view.SearchFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class SearchViewModel @Inject constructor(
    private val mApp: Application,
    private val respository: SearchRespository
) : AndroidViewModel(mApp) {

    private val searchTypesArray = enumValues<SearchFragment.SearchType>()
    private var selectedSearchTypeIndex = 0

    fun selectNextSearchType()
    {
        selectedSearchTypeIndex++
        selectedSearchTypeIndex %= searchTypesArray.size
        _currentSearchType.value = searchTypesArray[selectedSearchTypeIndex]
    }

    private val _currentSearchType = MutableStateFlow(searchTypesArray[selectedSearchTypeIndex])
    val currentSearchType = _currentSearchType.asStateFlow()

    private var lastSearchType: SearchFragment.SearchType? = null

    var currentQuery: String? = null
        private set

    private var currentSearchResult: Flow<SearchStatus>? = null

    fun search(query: String): Flow<SearchStatus>
    {
        val searchType = _currentSearchType.value
        Log.d("TAG","Submit new query `$query`. Type $searchType")

        val lastResult = currentSearchResult

        if (query == currentQuery && _currentSearchType.value == lastSearchType && lastResult != null)
        {
            Log.d("TAG","Returning last result")
            return lastResult
        }

        currentQuery = query
        lastSearchType = _currentSearchType.value


        val newResult: Flow<SearchStatus> = when (searchType)
        {
            SearchFragment.SearchType.USER -> respository.searchUser(query)
            SearchFragment.SearchType.TAG -> respository.searchTag(query)
        }


        currentSearchResult = newResult
        return newResult
    }

    fun clearQuery()
    {
        currentQuery = null
    }
}