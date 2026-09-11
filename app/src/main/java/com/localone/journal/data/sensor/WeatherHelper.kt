package com.localone.journal.data.sensor

import com.localone.journal.domain.model.EntryWeather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object WeatherHelper {

    suspend fun fetchWeather(latitude: Double, longitude: Double): EntryWeather? = withContext(Dispatchers.IO) {
        val urlStr = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current_weather=true"
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlStr)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val current = json.getJSONObject("current_weather")
                val temp = current.getDouble("temperature")
                val code = current.getInt("weathercode")

                val (desc, emoji) = parseWmoWeatherCode(code)

                return@withContext EntryWeather(
                    temperatureCelsius = temp,
                    conditionsDescription = "$desc $emoji",
                    weatherCode = code.toString(),
                    moonPhase = 0.5
                )
            }
        } catch (e: Exception) {
            // 网络异常时容错
        } finally {
            connection?.disconnect()
        }
        null
    }

    fun parseWmoWeatherCode(code: Int): Pair<String, String> {
        return when (code) {
            0 -> "晴朗" to "☀"
            1 -> "主要晴朗" to "🌤"
            2 -> "局部多云" to "⛅"
            3 -> "阴天" to "☁"
            45, 48 -> "有雾" to "🌫"
            51, 53, 55 -> "毛毛雨" to "🌦"
            56, 57 -> "冻毛毛雨" to "🌧"
            61, 63 -> "中雨" to "🌧"
            65 -> "大雨" to "🌧"
            66, 67 -> "冻雨" to "🌨"
            71, 73, 75 -> "降雪" to "❄"
            77 -> "雪粒" to "🌨"
            80, 81, 82 -> "阵雨" to "🌧"
            85, 86 -> "阵雪" to "❄"
            95 -> "雷阵雨" to "⛈"
            96, 99 -> "雷暴伴有冰雹" to "⛈"
            else -> "晴朗" to "☀"
        }
    }
}
