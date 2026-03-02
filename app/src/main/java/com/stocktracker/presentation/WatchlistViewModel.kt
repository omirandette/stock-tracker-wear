package com.stocktracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stocktracker.data.repository.StockRepository
import com.stocktracker.model.SearchResult
import com.stocktracker.model.Stock
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WatchlistViewModel(
    private val repository: StockRepository,
) : ViewModel() {

    val stocks: StateFlow<List<Stock>> = repository.watchAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    init {
        @OptIn(FlowPreview::class)
        _searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.length < 2) {
                    _searchResults.update { emptyList() }
                    _isSearching.update { false }
                    return@onEach
                }
                _isSearching.update { true }
                try {
                    _searchResults.update { repository.searchStocks(query) }
                } catch (_: Exception) {
                    _searchResults.update { emptyList() }
                } finally {
                    _isSearching.update { false }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.update { query }
    }

    fun clearSearch() {
        _searchQuery.update { "" }
        _searchResults.update { emptyList() }
        _isSearching.update { false }
    }

    fun addStock(symbol: String) {
        viewModelScope.launch {
            _isLoading.update { true }
            try {
                repository.addStock(symbol)
                _error.update { null }
            } catch (e: Exception) {
                _error.update { "Failed to add $symbol" }
            } finally {
                _isLoading.update { false }
            }
        }
    }

    fun removeStock(symbol: String) {
        viewModelScope.launch {
            repository.removeStock(symbol)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.update { true }
            try {
                repository.refreshAll()
                _error.update { null }
            } catch (e: Exception) {
                _error.update { "Refresh failed" }
            } finally {
                _isLoading.update { false }
            }
        }
    }

    class Factory(
        private val repository: StockRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WatchlistViewModel(repository) as T
    }
}
