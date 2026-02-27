package com.managerbatching.batchmanager.model

import java.util.UUID

enum class BatchStatus { ACTIVE, PENDING, COMPLETED, PROBLEMATIC }
enum class EggType { CHICKEN, QUAIL, DUCK, GOOSE, TURKEY }

data class Batch(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val eggType: EggType = EggType.CHICKEN,
    val totalEggs: Int = 0,
    val startDate: Long = System.currentTimeMillis(),
    val status: BatchStatus = BatchStatus.PENDING,
    val events: List<BatchEvent> = emptyList(),
    val incubationParams: IncubationParams = IncubationParams(),
    val successCount: Int = 0,
    val discardCount: Int = 0,
    val notes: String = ""
)

data class IncubationParams(
    val temperature: Float = 37.5f,
    val humidity: Float = 60f,
    val turnFrequencyHours: Int = 8,
    val incubationDays: Int = 21,
    val incubationStartDate: Long? = null
)

data class BatchEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: EventType = EventType.NOTE,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val quantity: Int? = null
)

enum class EventType(val label: String, val emoji: String) {
    EGGS_ADDED("Eggs Added", "🥚"),
    EGGS_DISCARDED("Eggs Discarded", "❌"),
    HUMIDITY_CHANGED("Humidity Changed", "💧"),
    TURNED("Eggs Turned", "🔄"),
    INCUBATION_STARTED("Incubation Started", "🌡️"),
    TEMPERATURE_CHANGED("Temperature Changed", "🌡️"),
    CANDLING_DONE("Candling Done", "🕯️"),
    HATCHING_STARTED("Hatching Started", "🐣"),
    BATCH_COMPLETED("Batch Completed", "✅"),
    NOTE("Note", "📝")
}