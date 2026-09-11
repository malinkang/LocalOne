package com.localone.journal.domain.model

data class EntryLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val placeName: String? = null,
    val localityName: String? = null,
    val administrativeArea: String? = null,
    val country: String? = null
) {
    val displaySummary: String
        get() = placeName ?: localityName ?: administrativeArea ?: "未知位置"
}

data class EntryWeather(
    val temperatureCelsius: Double,
    val conditionsDescription: String,
    val weatherCode: String,
    val moonPhase: Double? = null,
    val moonPhaseCode: String? = null,
    val relativeHumidity: Double? = null
)

data class EntryActivity(
    val activityName: String? = "Stationary",
    val stepCount: Int? = null
)
