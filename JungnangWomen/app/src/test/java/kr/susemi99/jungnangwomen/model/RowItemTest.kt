package kr.susemi99.jungnangwomen.model

import kr.susemi99.jungnangwomen.rowItem
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class RowItemTest {
  private val monday = LocalDate.of(2026, 3, 2)
  private val friday = LocalDate.of(2026, 3, 6)
  private lateinit var originalLocale: Locale

  // 천 단위 구분 기호가 기본 로캘을 따르므로 고정한다.
  @Before
  fun setUp() {
    originalLocale = Locale.getDefault()
    Locale.setDefault(Locale.KOREA)
  }

  @After
  fun tearDown() {
    Locale.setDefault(originalLocale)
  }

  @Test
  fun `key는 기관 코드와 강좌 코드를 합친다`() {
    assertEquals("25_C1", rowItem(classCode = "C1").key)
  }

  @Test
  fun `난이도는 대괄호로 감싼다`() {
    assertEquals("[초급]", rowItem().difficulty)
  }

  @Test
  fun `신청 기간이 하루면 날짜를 한 번만 표시한다`() {
    assertEquals(
      "2026-03-02(월) 09:00 ~ 18:00",
      rowItem(receiveFrom = monday, receiveTo = monday).receivePeriod,
    )
  }

  @Test
  fun `신청 기간이 여러 날이면 두 줄로 표시한다`() {
    assertEquals(
      "2026-03-02(월) 09:00 ~\n2026-03-06(금) 18:00",
      rowItem(receiveFrom = monday, receiveTo = friday).receivePeriod,
    )
  }

  @Test
  fun `하루 교육은 날짜와 시간을 한 줄로 표시하고 요일 플래그는 쓰지 않는다`() {
    assertEquals(
      "2026-03-02(월) 14:00 ~ 17:00",
      rowItem(educateFrom = monday, educateTo = monday, monday = "Y").educateSchedule,
    )
  }

  @Test
  fun `기기 언어가 영어여도 요일은 한국어로 표시한다`() {
    Locale.setDefault(Locale.US)

    assertEquals("2026-03-02(월) 14:00 ~ 17:00", rowItem(educateFrom = monday, educateTo = monday).educateSchedule)
  }

  @Test
  fun `여러 날 교육은 기간과 값이 Y인 요일, 시간을 줄을 나눠 표시한다`() {
    assertEquals(
      "2026-03-02(월) ~ 2026-03-06(금)\n월수금\n14:00 ~ 17:00",
      rowItem(educateFrom = monday, educateTo = friday, monday = "Y", tuesday = " ", wednesday = "Y", friday = "Y").educateSchedule,
    )
  }

  // API는 진행하지 않는 요일을 "N"으로 보낸다.
  @Test
  fun `진행하지 않는 요일 N은 표시하지 않는다`() {
    assertEquals(
      "2026-03-02(월) ~ 2026-03-06(금)\n월화수목금\n14:00 ~ 17:00",
      rowItem(
        educateFrom = monday,
        educateTo = friday,
        monday = "Y",
        tuesday = "Y",
        wednesday = "Y",
        thursday = "Y",
        friday = "Y",
        saturday = "N",
        sunday = "N",
      ).educateSchedule,
    )
  }

  @Test
  fun `잔여 인원은 정수로 표시한다`() {
    assertEquals("5/20명", rowItem(spareNum = 5f, collectNum = 20f).remainNumber)
  }

  @Test
  fun `수강료가 0원이면 무료로 표시한다`() {
    assertEquals("무료", rowItem(educateFee = 0f).fee)
  }

  @Test
  fun `수강료는 천 단위로 구분한다`() {
    assertEquals("100,000원", rowItem(educateFee = 100000f).fee)
  }

  @Test
  fun `접수 방법을 조합해 표시한다`() {
    assertEquals("방문, 온라인", rowItem(visitReceiveFlag = "Y", onlineReceiveFlag = "Y").howToRegister)
    assertEquals("방문", rowItem(visitReceiveFlag = "Y", onlineReceiveFlag = "N").howToRegister)
    assertEquals("온라인", rowItem(visitReceiveFlag = "N", onlineReceiveFlag = "Y").howToRegister)
    assertEquals("접수방법 확인", rowItem(visitReceiveFlag = "N", onlineReceiveFlag = "N").howToRegister)
  }

  @Test
  fun `접수 플래그가 공백이어도 안내 문구를 표시한다`() {
    assertEquals("접수방법 확인", rowItem(visitReceiveFlag = " ", onlineReceiveFlag = " ").howToRegister)
  }
}
