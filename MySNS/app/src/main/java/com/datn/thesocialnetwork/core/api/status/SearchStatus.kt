package com.datn.thesocialnetwork.core.api.status

import com.datn.thesocialnetwork.data.repository.model.SearchModel

sealed class SearchStatus
{
    object Loading : SearchStatus()
    data class Success(val result: List<SearchModel>) : SearchStatus()
    object Interrupted : SearchStatus()
}
