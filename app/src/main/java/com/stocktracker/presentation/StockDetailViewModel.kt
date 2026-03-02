package com.stocktracker.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stocktracker.data.repository.StockRepository
import com.stocktracker.model.ChartData
import com.stocktracker.model.Stock
import com.stocktracker.model.TimePeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StockDetailViewModel(
    private val repository: StockRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    val stocks: StateFlow<List<Stock>> = repository.watchAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class CacheEntry(val data: ChartData, val timestamp: Long)

    private val chartCache = mutableMapOf<String, CacheEntry>()

    private val _chartData = MutableStateFlow(ChartData(emptyList(), 0.0, 0.0))
    val chartData: StateFlow<ChartData> = _chartData

    private val _isChartLoading = MutableStateFlow(false)
    val isChartLoading: StateFlow<Boolean> = _isChartLoading

    private val _chartError = MutableStateFlow<String?>(null)
    val chartError: StateFlow<String?> = _chartError

    fun loadChart(symbol: String, period: TimePeriod, forceRefresh: Boolean = false) {
        val cacheKey = "$symbol-${period.name}"
        if (!forceRefresh) {
            val cached = chartCache[cacheKey]
            if (cached != null && !isStale(cached.timestamp)) {
                _chartData.value = cached.data
                _chartError.value = null
                return
            }
        }

        viewModelScope.launch {
            _isChartLoading.value = true
            _chartError.value = null
            try {
                val data = repository.getChartData(symbol, period)
                chartCache[cacheKey] = CacheEntry(data, clock())
                _chartData.value = data
            } catch (e: Exception) {
                Log.e("StockChart", "Failed to load chart for $symbol $period", e)
                _chartError.value = "Failed to load chart"
                _chartData.value = ChartData(emptyList(), 0.0, 0.0)
            } finally {
                _isChartLoading.value = false
            }
        }
    }

    private fun isStale(timestamp: Long): Boolean =
        clock() - timestamp >= CACHE_TTL_MS

    class Factory(
        private val repository: StockRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StockDetailViewModel(repository) as T
    }

    companion object {
        internal const val CACHE_TTL_MS = 300_000L // 5 minutes
    }
}
