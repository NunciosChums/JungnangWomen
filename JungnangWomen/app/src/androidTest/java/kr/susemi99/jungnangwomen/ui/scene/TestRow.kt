package kr.susemi99.jungnangwomen.ui.scene

import kr.susemi99.jungnangwomen.model.RowItem
import java.time.LocalDate
import java.time.LocalTime

fun testRow(classCode: String = "C1", className: String = "테스트 강좌", url: String = "https://example.com/class") = RowItem(
  classCode = classCode,
  className = className,
  organCode = "25",
  organName = "중랑여성인력개발센터",
  difficultyCode = "J",
  difficultyName = "초급",
  receiveFrom = LocalDate.of(2026, 3, 2),
  receiveTo = LocalDate.of(2026, 3, 6),
  receiveTimeFrom = LocalTime.of(9, 0),
  receiveTimeTo = LocalTime.of(18, 0),
  educateFrom = LocalDate.of(2026, 3, 9),
  educateTo = LocalDate.of(2026, 3, 13),
  educateTimeFrom = LocalTime.of(14, 0),
  educateTimeTo = LocalTime.of(17, 0),
  monday = "Y",
  tuesday = null,
  wednesday = "Y",
  thursday = null,
  friday = "Y",
  saturday = null,
  sunday = null,
  collectNum = 20f,
  spareNum = 5f,
  educateFee = 100000f,
  visitReceiveFlag = "Y",
  onlineReceiveFlag = "Y",
  url = url,
)
