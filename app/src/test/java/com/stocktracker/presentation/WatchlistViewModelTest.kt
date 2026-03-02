package com.stocktracker.presentation

import com.stocktracker.data.repository.StockRepository
import com.stocktracker.model.SearchResult
import com.stocktracker.model.Stock
import com.stocktracker.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistViewModelTest {

    @get:Rule val dispatcherRule = MainDispatcherRule()

    private val repository = mockk<StockRepository>(relaxed = true)
    private val vm by lazy { WatchlistViewModel(repository) }

    @Test
    fun `addStock sets loading then clears on success`() = runTest {
        coEvery { repository.addStock("AAPL") } returns Unit
        vm.addStock("AAPL")
        advanceUntilIdle()
        assertFalse(vm.isLoading.value)
        assertNull(vm.error.value)
    }

    @Test
    fun `addStock sets error on failure`() = runTest {
        coEvery { repository.addStock("BAD") } throws RuntimeException("fail")
        vm.addStock("BAD")
        advanceUntilIdle()
        assertEquals("Failed to add BAD", vm.error.value)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `removeStock delegates to repository`() = runTest {
        vm.removeStock("AAPL")
        advanceUntilIdle()
        coVerify { repository.removeStock("AAPL") }
    }

    @Test
    fun `refresh sets loading then clears on success`() = runTest {
        coEvery { repository.refreshAll() } returns Unit
        vm.refresh()
        advanceUntilIdle()
        assertFalse(vm.isLoading.value)
        assertNull(vm.error.value)
    }

    @Test
    fun `refresh sets error on failure`() = runTest {
        coEvery { repository.refreshAll() } throws RuntimeException("fail")
        vm.refresh()
        advanceUntilIdle()
        assertEquals("Refresh failed", vm.error.value)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `refreshIfStale does nothing when stocks are empty`() = runTest {
        vm.refreshIfStale()
        advanceUntilIdle()
        coVerify(exactly = 0) { repository.refreshAll() }
    }

    @Test
    fun `refreshIfStale refreshes when data is stale`() = runTest {
        val staleStock = Stock("AAPL", 150.0, 2.0, "1.35%",
            lastUpdated = System.currentTimeMillis() - WatchlistViewModel.STALE_THRESHOLD_MS - 1000)
        val stocksFlow = MutableStateFlow(listOf(staleStock))
        every { repository.watchAll() } returns stocksFlow
        coEvery { repository.refreshAll() } returns Unit
        val staleVm = WatchlistViewModel(repository)
        val job = backgroundScope.launch { staleVm.stocks.collect {} }

        advanceUntilIdle()
        staleVm.refreshIfStale()
        advanceUntilIdle()
        coVerify { repository.refreshAll() }
        job.cancel()
    }

    @Test
    fun `refreshIfStale does not refresh when data is fresh`() = runTest {
        val freshStock = Stock("AAPL", 150.0, 2.0, "1.35%",
            lastUpdated = System.currentTimeMillis())
        val stocksFlow = MutableStateFlow(listOf(freshStock))
        every { repository.watchAll() } returns stocksFlow
        val freshVm = WatchlistViewModel(repository)
        val job = backgroundScope.launch { freshVm.stocks.collect {} }

        advanceUntilIdle()
        freshVm.refreshIfStale()
        advanceUntilIdle()
        coVerify(exactly = 0) { repository.refreshAll() }
        job.cancel()
    }

    @Test
    fun `onSearchQueryChange updates searchQuery state`() = runTest {
        vm.onSearchQueryChange("AAPL")
        assertEquals("AAPL", vm.searchQuery.value)
    }

    @Test
    fun `search returns results after debounce for 2+ char query`() = runTest {
        val results = listOf(SearchResult("AAPL", "Apple Inc.", "NASDAQ"))
        coEvery { repository.searchStocks("AP") } returns results

        vm.onSearchQueryChange("AP")
        advanceTimeBy(350)
        advanceUntilIdle()

        assertEquals(results, vm.searchResults.value)
        assertFalse(vm.isSearching.value)
    }

    @Test
    fun `search clears results for query shorter than 2 chars`() = runTest {
        val results = listOf(SearchResult("AAPL", "Apple Inc.", "NASDAQ"))
        coEvery { repository.searchStocks("AP") } returns results

        vm.onSearchQueryChange("AP")
        advanceTimeBy(350)
        advanceUntilIdle()
        assertTrue(vm.searchResults.value.isNotEmpty())

        vm.onSearchQueryChange("A")
        advanceTimeBy(350)
        advanceUntilIdle()
        assertTrue(vm.searchResults.value.isEmpty())
    }

    @Test
    fun `search handles exception gracefully`() = runTest {
        coEvery { repository.searchStocks("XX") } throws RuntimeException("network error")

        vm.onSearchQueryChange("XX")
        advanceTimeBy(350)
        advanceUntilIdle()

        assertTrue(vm.searchResults.value.isEmpty())
        assertFalse(vm.isSearching.value)
    }

    @Test
    fun `clearSearch resets query results and searching state`() = runTest {
        val results = listOf(SearchResult("AAPL", "Apple Inc.", "NASDAQ"))
        coEvery { repository.searchStocks("AP") } returns results

        vm.onSearchQueryChange("AP")
        advanceTimeBy(350)
        advanceUntilIdle()

        vm.clearSearch()
        assertEquals("", vm.searchQuery.value)
        assertTrue(vm.searchResults.value.isEmpty())
        assertFalse(vm.isSearching.value)
    }
}
