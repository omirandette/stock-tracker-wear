package com.stocktracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stocktracker.data.repository.StockRepository
import com.stocktracker.model.ChartPoint
import com.stocktracker.model.Stock
import com.stocktracker.model.TimePeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StockDetailViewModel(
    private val repository: StockRepository,
    private val apiKey: String,
) : ViewModel() {

    val stocks: StateFlow<List<Stock>> = repository.watchAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val chartCache = mutableMapOf<String, List<ChartPoint>>()

    private val _chartData = MutableStateFlow<List<ChartPoint>>(emptyList())
    val chartData: StateFlow<List<ChartPoint>> = _chartData

    private val _isChartLoading = MutableStateFlow(false)
    val isChartLoading: StateFlow<Boolean> = _isChartLoading

    private val _chartError = MutableStateFlow<String?>(null)
    val chartError: StateFlow<String?> = _chartError

    fun loadChart(symbol: String, period: TimePeriod) {
        val cacheKey = "$symbol-${period.name}"
        val cached = chartCache[cacheKey]
        if (cached != null) {
            _chartData.value = cached
            _chartError.value = null
            return
        }

        viewModelScope.launch {
            _isChartLoading.value = true
            _chartError.value = null
            try {
                val data = repository.getChartData(symbol, period, apiKey)
                chartCache[cacheKey] = data
                _chartData.value = data
            } catch (e: Exception) {
                _chartError.value = "Failed to load chart"
                _chartData.value = emptyList()
            } finally {
                _isChartLoading.value = false
            }
        }
    }

    class Factory(
        private val repository: StockRepository,
        private val apiKey: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StockDetailViewModel(repository, apiKey) as T
    }
}
