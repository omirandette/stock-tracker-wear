package com.stocktracker.presentation

import com.stocktracker.data.repository.StockRepository
import com.stocktracker.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
}
