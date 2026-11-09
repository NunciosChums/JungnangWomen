package kr.susemi99.jungnangwomen.api

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kr.susemi99.jungnangwomen.apiRespondingWith
import kr.susemi99.jungnangwomen.fixture
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/** 실제 API 응답을 앱과 같은 Retrofit·Json 설정으로 처리해 라이브러리 업그레이드 영향을 확인한다. */
class ApiContractTest {
  @Test
  fun `요청 URL에 서비스 이름과 양끝 인덱스가 들어간다`() = runBlocking {
    var request: Request? = null
    val api = apiRespondingWith(fixture("list_success.json")) { request = it }

    api.list(startIndex = 1, endIndex = 30)

    val url = checkNotNull(request).url
    assertEquals("openapi.seoul.go.kr", url.host)
    assertEquals(8088, url.port)
    assertTrue(url.encodedPath.endsWith("/json/SeoulJungNangWomenResourcesClass/1/30"))
  }

  @Test
  fun `성공 응답의 모든 형식을 파싱한다`() = runBlocking {
    val response = apiRespondingWith(fixture("list_success.json")).list(1, 3)

    val classItem = checkNotNull(response.classItem)
    assertEquals(29, classItem.listTotalCount)
    assertEquals("INFO-000", classItem.result.code)
    assertEquals(3, classItem.rows.size)

    val row = classItem.rows.first()
    assertEquals("C100164741", row.classCode)
    assertEquals("병원동행매니저", row.className)
    assertEquals("25", row.organCode)
    assertEquals("관계없음", row.difficultyName)
    assertEquals(LocalDate.of(2026, 11, 2), row.receiveFrom)
    assertEquals(LocalDate.of(2027, 1, 11), row.receiveTo)
    assertEquals(LocalTime.of(9, 0), row.receiveTimeFrom)
    assertEquals(LocalTime.of(18, 0), row.receiveTimeTo)
    assertEquals(LocalDate.of(2027, 1, 12), row.educateFrom)
    assertEquals(LocalDate.of(2027, 2, 16), row.educateTo)
    assertEquals(LocalTime.of(14, 0), row.educateTimeFrom)
    assertEquals(LocalTime.of(17, 0), row.educateTimeTo)
    assertEquals("Y", row.monday)
    assertEquals("N", row.saturday)
    assertEquals(20f, row.collectNum)
    assertEquals(0f, row.spareNum)
    assertEquals(100000f, row.educateFee)
    assertEquals("Y", row.visitReceiveFlag)
    assertEquals("Y", row.onlineReceiveFlag)
    assertEquals(
      "https://jungnang.seoulcareerup.or.kr/jungnang/edu/selectEduProgram.do?sch_class_code=C100164741",
      row.url,
    )
  }

  @Test
  fun `날짜나 시간이 빈 값인 행만 제외하고 나머지 행은 파싱한다`() = runBlocking {
    val original = fixture("list_success.json")
    val originalRows = checkNotNull(apiRespondingWith(original).list(1, 3).classItem).rows

    for (field in listOf("\"RECEIVE_FROM\":\"20261102\"", "\"EDUCATE_TIME_FROM\":\"14:00\"")) {
      val broken = original.replaceFirst(field, field.substringBefore(':') + ":\"\"")

      val classItem = checkNotNull(apiRespondingWith(broken).list(1, 3).classItem)

      assertEquals(field, originalRows.drop(1), classItem.rows)
      assertEquals(29, classItem.listTotalCount)
    }
  }

  @Test
  fun `필수 항목이 없는 행도 제외한다`() = runBlocking {
    val row = """{"CLASS_CODE":"C1"}"""
    val body = """{"SeoulJungNangWomenResourcesClass":{"list_total_count":1,"RESULT":{"CODE":"INFO-000","MESSAGE":"OK"},"row":[$row]}}"""

    val classItem = checkNotNull(apiRespondingWith(body).list(1, 30).classItem)

    assertTrue(classItem.rows.isEmpty())
  }

  @Test
  fun `결과 없음 응답은 최상위 RESULT로 파싱한다`() = runBlocking {
    val response = apiRespondingWith(fixture("list_no_data.json")).list(99901, 99930)

    assertNull(response.classItem)
    assertEquals("INFO-200", response.result?.code)
  }

  @Test
  fun `XML 오류 응답은 최상위 RESULT로 변환한다`() = runBlocking {
    val response = apiRespondingWith(fixture("list_invalid_key.xml")).list(1, 30)

    assertNull(response.classItem)
    val result = checkNotNull(response.result)
    assertEquals("INFO-100", result.code)
    assertTrue(result.message.startsWith("인증키가 유효하지 않습니다."))
    assertTrue(result.message.endsWith("신청하십시오."))
  }

  @Test
  fun `요청 범위 초과 XML 오류도 코드를 읽는다`() = runBlocking {
    val response = apiRespondingWith(fixture("list_error.xml")).list(1, 2000)

    assertEquals("ERROR-336", response.result?.code)
  }

  @Test
  fun `형식을 알 수 없는 응답은 SerializationException을 던진다`() {
    assertThrows(SerializationException::class.java) {
      runBlocking { apiRespondingWith("<html>Bad Gateway</html>").list(1, 30) }
    }
  }
}
