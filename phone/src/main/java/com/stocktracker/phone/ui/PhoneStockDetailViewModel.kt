package com.stocktracker.phone.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stocktracker.shared.data.repository.StockRepository
import com.stocktracker.shared.model.ChartData
import com.stocktracker.shared.model.TimePeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PhoneStockDetailViewModel(
    private val repository: StockRepository,
) : ViewModel() {

    private val _chartData = MutableStateFlow(ChartData(emptyList(), 0.0, 0.0))
    val chartData: StateFlow<ChartData> = _chartData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val cache = mutableMapOf<Pair<String, TimePeriod>, ChartData>()

    fun loadChart(symbol: String, period: TimePeriod, forceRefresh: Boolean = false) {
        val cacheKey = symbol to period
        if (!forceRefresh) {
            cache[cacheKey]?.let {
                _chartData.value = it
                _error.value = null
                return
            }
        }
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            try {
                val data = repository.getChartData(symbol, period)
                cache[cacheKey] = data
                _chartData.value = data
            } catch (e: Exception) {
                _error.update { "Chart load failed" }
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
            PhoneStockDetailViewModel(repository) as T
    }
}
