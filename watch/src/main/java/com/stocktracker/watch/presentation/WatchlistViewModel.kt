package com.stocktracker.watch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stocktracker.shared.data.repository.StockRepository
import com.stocktracker.shared.model.Stock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
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

    suspend fun refreshIfStale() {
        val currentStocks = repository.watchAll().firstOrNull() ?: return
        val oldest = currentStocks.minOfOrNull { it.lastUpdated } ?: return
        if (System.currentTimeMillis() - oldest > STALE_THRESHOLD_MS) {
            refresh()
        }
    }

    companion object {
        const val STALE_THRESHOLD_MS = 5 * 60 * 1000L
    }

    class Factory(
        private val repository: StockRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WatchlistViewModel(repository) as T
    }
}
