package com.managerbatching.batchmanager.data

import com.managerbatching.batchmanager.model.Batch
import com.managerbatching.batchmanager.model.BatchEvent
import com.managerbatching.batchmanager.model.BatchStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BatchRepository(private val prefs: PreferencesManager) {

    private val _batches = MutableStateFlow<List<Batch>>(emptyList())
    val batches: StateFlow<List<Batch>> = _batches.asStateFlow()

    init {
        _batches.value = prefs.loadBatches()
    }

    fun addBatch(batch: Batch) {
        val updated = _batches.value + batch
        _batches.value = updated
        prefs.saveBatches(updated)
    }

    fun updateBatch(batch: Batch) {
        val updated = _batches.value.map { if (it.id == batch.id) batch else it }
        _batches.value = updated
        prefs.saveBatches(updated)
    }

    fun deleteBatch(batchId: String) {
        val updated = _batches.value.filter { it.id != batchId }
        _batches.value = updated
        prefs.saveBatches(updated)
    }

    fun addEventToBatch(batchId: String, event: BatchEvent) {
        val batch = _batches.value.find { it.id == batchId } ?: return
        val updated = batch.copy(events = batch.events + event)
        updateBatch(updated)
    }

    fun getBatch(batchId: String): Batch? = _batches.value.find { it.id == batchId }

    fun clearHistory() {
        val active = _batches.value.filter { it.status != BatchStatus.COMPLETED }
        _batches.value = active
        prefs.saveBatches(active)
    }
}