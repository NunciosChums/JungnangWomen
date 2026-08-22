package kr.susemi99.jungnangwomen.api

/** 서울 열린데이터광장 API가 HTTP 200으로 응답했지만 `RESULT.CODE`가 오류인 경우 */
class ApiException(val code: String, message: String) : Exception("[$code] $message")
