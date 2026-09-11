package com.localone.journal.data.sensor

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherHelperTest {

    @Test
    fun testWmoWeatherCodeParsing() {
        val (descClear, emojiClear) = WeatherHelper.parseWmoWeatherCode(0)
        assertEquals("晴朗", descClear)
        assertEquals("☀", emojiClear)

        val (descCloudy, emojiCloudy) = WeatherHelper.parseWmoWeatherCode(2)
        assertEquals("局部多云", descCloudy)
        assertEquals("⛅", emojiCloudy)

        val (descRain, emojiRain) = WeatherHelper.parseWmoWeatherCode(61)
        assertEquals("中雨", descRain)
        assertEquals("🌧", emojiRain)

        val (descSnow, emojiSnow) = WeatherHelper.parseWmoWeatherCode(71)
        assertEquals("降雪", descSnow)
        assertEquals("❄", emojiSnow)

        val (descThunder, emojiThunder) = WeatherHelper.parseWmoWeatherCode(95)
        assertEquals("雷阵雨", descThunder)
        assertEquals("⛈", emojiThunder)
    }
}
