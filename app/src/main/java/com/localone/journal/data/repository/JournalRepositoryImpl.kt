package com.localone.journal.data.repository

import com.localone.journal.data.local.dao.JournalDao
import com.localone.journal.data.local.entity.JournalEntryEntity
import com.localone.journal.domain.model.*
import com.localone.journal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

class JournalRepositoryImpl(
    private val dao: JournalDao
) : JournalRepository {

    override fun getAllEntries(): Flow<List<JournalEntry>> {
        return dao.getAllActiveEntries().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getOnThisDayEntries(month: Int, day: Int): Flow<List<JournalEntry>> {
        return dao.getOnThisDayEntries(month, day).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getEntryById(id: Long): JournalEntry? {
        return dao.getEntryById(id)?.toDomain()
    }

    override suspend fun insertOrUpdateEntry(entry: JournalEntry): Long {
        return dao.insertOrUpdate(entry.toEntity())
    }

    override suspend fun deleteEntry(id: Long) {
        dao.softDelete(id)
    }

    override suspend fun toggleStar(id: Long, isStarred: Boolean) {
        dao.updateStarStatus(id, isStarred)
    }

    override fun searchEntries(query: String): Flow<List<JournalEntry>> {
        return dao.searchEntries(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllBooks(): Flow<List<JournalBook>> {
        return flowOf(
            listOf(
                JournalBook(id = "default", name = "日常随笔", colorHex = "#44C0FF", isDefault = true),
                JournalBook(id = "work", name = "工作与思考", colorHex = "#FF6E6B", isDefault = false)
            )
        )
    }

    private fun JournalEntryEntity.toDomain(): JournalEntry {
        val photos = jsonToStringList(photoUrisJson)
        val audios = jsonToStringList(audioUrisJson)
        val tagList = jsonToStringList(tagsJson)
        val loc = locationJson?.let { parseLocation(it) }
        val wth = weatherJson?.let { parseWeather(it) }
        val act = activityJson?.let { parseActivity(it) }

        return JournalEntry(
            id = id,
            uuid = uuid,
            bookId = bookId,
            creationTime = Instant.ofEpochMilli(creationEpochMs),
            modifiedTime = Instant.ofEpochMilli(modifiedEpochMs),
            timeZone = timeZone,
            title = title,
            content = content,
            previewSnippet = previewSnippet,
            photoUris = photos,
            audioUris = audios,
            tags = tagList,
            location = loc,
            weather = wth,
            activity = act,
            isStarred = isStarred,
            isPinned = isPinned,
            isDraft = isDraft,
            isTrashed = isTrashed
        )
    }

    private fun JournalEntry.toEntity(): JournalEntryEntity {
        return JournalEntryEntity(
            id = id,
            uuid = uuid,
            bookId = bookId,
            creationEpochMs = creationTime.toEpochMilli(),
            modifiedEpochMs = modifiedTime.toEpochMilli(),
            timeZone = timeZone,
            year = year,
            month = month,
            day = dayOfMonth,
            title = title,
            content = content,
            previewSnippet = previewSnippet.ifBlank { content.take(120).replace("\n", " ") },
            photoUrisJson = stringListToJson(photoUris),
            audioUrisJson = stringListToJson(audioUris),
            tagsJson = stringListToJson(tags),
            locationJson = location?.let { loc ->
                JSONObject().apply {
                    put("latitude", loc.latitude)
                    put("longitude", loc.longitude)
                    put("altitude", loc.altitude ?: JSONObject.NULL)
                    put("placeName", loc.placeName ?: JSONObject.NULL)
                    put("localityName", loc.localityName ?: JSONObject.NULL)
                    put("country", loc.country ?: JSONObject.NULL)
                }.toString()
            },
            weatherJson = weather?.let { w ->
                JSONObject().apply {
                    put("temp", w.temperatureCelsius)
                    put("desc", w.conditionsDescription)
                    put("code", w.weatherCode)
                    put("moonPhase", w.moonPhase ?: JSONObject.NULL)
                }.toString()
            },
            activityJson = activity?.let { a ->
                JSONObject().apply {
                    put("name", a.activityName ?: "Stationary")
                    put("steps", a.stepCount ?: JSONObject.NULL)
                }.toString()
            },
            isStarred = isStarred,
            isPinned = isPinned,
            isDraft = isDraft,
            isTrashed = isTrashed
        )
    }

    private fun stringListToJson(list: List<String>): String {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    private fun jsonToStringList(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<String>()
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            list.add(arr.getString(i))
        }
        return list
    }

    private fun parseLocation(json: String): EntryLocation {
        val obj = JSONObject(json)
        return EntryLocation(
            latitude = obj.getDouble("latitude"),
            longitude = obj.getDouble("longitude"),
            altitude = if (obj.isNull("altitude")) null else obj.getDouble("altitude"),
            placeName = if (obj.isNull("placeName")) null else obj.getString("placeName"),
            localityName = if (obj.isNull("localityName")) null else obj.getString("localityName"),
            country = if (obj.isNull("country")) null else obj.getString("country")
        )
    }

    private fun parseWeather(json: String): EntryWeather {
        val obj = JSONObject(json)
        return EntryWeather(
            temperatureCelsius = obj.getDouble("temp"),
            conditionsDescription = obj.getString("desc"),
            weatherCode = obj.getString("code"),
            moonPhase = if (obj.isNull("moonPhase")) null else obj.getDouble("moonPhase")
        )
    }

    private fun parseActivity(json: String): EntryActivity {
        val obj = JSONObject(json)
        return EntryActivity(
            activityName = obj.optString("name", "Stationary"),
            stepCount = if (obj.isNull("steps")) null else obj.getInt("steps")
        )
    }
}
