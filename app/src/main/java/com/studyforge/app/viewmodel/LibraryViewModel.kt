package com.studyforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.studyforge.app.data.LibraryFilterCriteria
import com.studyforge.app.data.LibraryPageQuery
import com.studyforge.app.data.QuestionLibraryRow
import com.studyforge.app.data.QuestionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.asStateFlow

/** Owns filter state and cancels obsolete database paging streams on criteria changes. */
class LibraryViewModel(repository: QuestionRepository) : ViewModel() {
    private val _criteria = MutableStateFlow(LibraryFilterCriteria())
    val criteria: StateFlow<LibraryFilterCriteria> = _criteria.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val rows = _criteria
        .flatMapLatest { criteria -> repository.pageLibrary(LibraryPageQuery.from(criteria)) }
        .cachedIn(viewModelScope)

    fun updateCriteria(criteria: LibraryFilterCriteria) {
        _criteria.value = criteria
    }

    fun updateQuery(query: String) {
        _criteria.value = _criteria.value.copy(query = query)
    }

    suspend fun countMatches(criteria: LibraryFilterCriteria = _criteria.value): Int =
        repository.countLibrary(LibraryPageQuery.from(criteria))

    suspend fun countTagUsage(tag: String): Int = repository.countTagUsage(tag)
}


class LibraryViewModelFactory(
    private val repository: QuestionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(LibraryViewModel::class.java))
        return LibraryViewModel(repository) as T
    }
}
