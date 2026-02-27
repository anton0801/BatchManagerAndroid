package com.managerbatching.batchmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.managerbatching.batchmanager.data.BatchRepository
import com.managerbatching.batchmanager.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BatchViewModel(private val repository: BatchRepository) : ViewModel() {

    val batches: StateFlow<List<Batch>> = repository.batches

    val activeBatches: StateFlow<List<Batch>> = batches
        .map { it.filter { b -> b.status == BatchStatus.ACTIVE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingBatches: StateFlow<List<Batch>> = batches
        .map { it.filter { b -> b.status == BatchStatus.PENDING } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedBatches: StateFlow<List<Batch>> = batches
        .map { it.filter { b -> b.status == BatchStatus.COMPLETED } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedBatchId = MutableStateFlow<String?>(null)
    val selectedBatch: StateFlow<Batch?> = combine(_selectedBatchId, batches) { id, list ->
        list.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectBatch(batchId: String) { _selectedBatchId.value = batchId }

    fun createBatch(batch: Batch) = viewModelScope.launch { repository.addBatch(batch) }

    fun updateBatch(batch: Batch) = viewModelScope.launch { repository.updateBatch(batch) }

    fun deleteBatch(batchId: String) = viewModelScope.launch { repository.deleteBatch(batchId) }

    fun addEvent(batchId: String, event: BatchEvent) = viewModelScope.launch {
        repository.addEventToBatch(batchId, event)
    }

    fun updateStatus(batchId: String, status: BatchStatus) = viewModelScope.launch {
        val batch = repository.getBatch(batchId) ?: return@launch
        repository.updateBatch(batch.copy(status = status))
    }

    fun startIncubation(batchId: String) = viewModelScope.launch {
        val batch = repository.getBatch(batchId) ?: return@launch
        val params = batch.incubationParams.copy(incubationStartDate = System.currentTimeMillis())
        repository.updateBatch(batch.copy(
            status = BatchStatus.ACTIVE,
            incubationParams = params
        ))
        repository.addEventToBatch(batchId, BatchEvent(
            type = EventType.INCUBATION_STARTED,
            description = "Incubation started"
        ))
    }

    fun markTurning(batchId: String) = viewModelScope.launch {
        repository.addEventToBatch(batchId, BatchEvent(
            type = EventType.TURNED,
            description = "Eggs turned"
        ))
    }

    fun clearHistory() = viewModelScope.launch { repository.clearHistory() }

    fun getDaysRemaining(batch: Batch): Int {
        val startDate = batch.incubationParams.incubationStartDate ?: return batch.incubationParams.incubationDays
        val elapsed = ((System.currentTimeMillis() - startDate) / (1000 * 60 * 60 * 24)).toInt()
        return maxOf(0, batch.incubationParams.incubationDays - elapsed)
    }

    fun getSuccessRate(batch: Batch): Float {
        if (batch.totalEggs == 0) return 0f
        return (batch.successCount.toFloat() / batch.totalEggs) * 100f
    }

    class Factory(private val repository: BatchRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BatchViewModel(repository) as T
    }
}