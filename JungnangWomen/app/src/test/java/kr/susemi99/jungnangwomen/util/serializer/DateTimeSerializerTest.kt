package kr.susemi99.jungnangwomen.util.serializer

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class DateTimeSerializerTest {
  @Test
  fun `날짜는 API 형식으로 읽고 같은 형식으로 쓴다`() {
    val date = LocalDate.of(2026, 3, 2)

    assertEquals(date, Json.decodeFromString(LocalDateSerializer, "\"20260302\""))
    assertEquals("\"20260302\"", Json.encodeToString(LocalDateSerializer, date))
  }

  @Test
  fun `시간은 API 형식으로 읽고 같은 형식으로 쓴다`() {
    val time = LocalTime.of(9, 0)

    assertEquals(time, Json.decodeFromString(LocalTimeSerializer, "\"09:00\""))
    assertEquals("\"09:00\"", Json.encodeToString(LocalTimeSerializer, time))
  }
}
