package kr.susemi99.jungnangwomen.extension

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// 함께 표시되는 교육 요일(월수금)이 한국어이므로 기기 언어와 관계없이 한국어로 고정한다.
private val DAY_NAME_FORMATTER = DateTimeFormatter.ofPattern("E", Locale.KOREAN)

/**
 * 요일 이름
 */
val LocalDate.dayName: String
  get() = format(DAY_NAME_FORMATTER)
