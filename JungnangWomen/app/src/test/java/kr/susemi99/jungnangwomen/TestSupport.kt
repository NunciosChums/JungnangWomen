package kr.susemi99.jungnangwomen

import kotlinx.serialization.ExperimentalSerializationApi
import kr.susemi99.jungnangwomen.api.Api
import kr.susemi99.jungnangwomen.di.NetworkModule
import kr.susemi99.jungnangwomen.model.RowItem
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.time.LocalDate
import java.time.LocalTime

/** `src/test/resources/fixtures` 에 저장한 실제 API 응답을 읽는다. */
fun fixture(name: String): String =
  checkNotNull(RowItem::class.java.classLoader?.getResourceAsStream("fixtures/$name")) { "fixture 없음: $name" }
    .bufferedReader()
    .use { it.readText() }

/**
 * 네트워크 없이 항상 [body]를 HTTP 200으로 돌려주는 [Api].
 * 앱과 같은 [NetworkModule] 설정을 쓰므로 Retrofit, converter, Json 경로 전체를 검증한다.
 */
@OptIn(ExperimentalSerializationApi::class)
fun apiRespondingWith(body: String, onRequest: (Request) -> Unit = {}): Api {
  val client = OkHttpClient.Builder()
    .addInterceptor { chain ->
      onRequest(chain.request())
      Response.Builder()
        .request(chain.request())
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .body(body.toResponseBody("application/json".toMediaType()))
        .build()
    }
    .build()
  val retrofit = NetworkModule.provideRetrofit(client, NetworkModule.provideConverterFactory())
  return NetworkModule.provideApiService(retrofit)
}

fun rowItem(
  classCode: String = "C1",
  receiveFrom: LocalDate = LocalDate.of(2026, 3, 2),
  receiveTo: LocalDate = LocalDate.of(2026, 3, 6),
  educateFrom: LocalDate = LocalDate.of(2026, 3, 2),
  educateTo: LocalDate = LocalDate.of(2026, 3, 6),
  monday: String? = null,
  tuesday: String? = null,
  wednesday: String? = null,
  thursday: String? = null,
  friday: String? = null,
  saturday: String? = null,
  sunday: String? = null,
  collectNum: Float = 20f,
  spareNum: Float = 5f,
  educateFee: Float = 0f,
  visitReceiveFlag: String = "Y",
  onlineReceiveFlag: String = "Y",
) = RowItem(
  classCode = classCode,
  className = "테스트 강좌",
  organCode = "25",
  organName = "중랑여성인력개발센터",
  difficultyCode = "J",
  difficultyName = "초급",
  receiveFrom = receiveFrom,
  receiveTo = receiveTo,
  receiveTimeFrom = LocalTime.of(9, 0),
  receiveTimeTo = LocalTime.of(18, 0),
  educateFrom = educateFrom,
  educateTo = educateTo,
  educateTimeFrom = LocalTime.of(14, 0),
  educateTimeTo = LocalTime.of(17, 0),
  monday = monday,
  tuesday = tuesday,
  wednesday = wednesday,
  thursday = thursday,
  friday = friday,
  saturday = saturday,
  sunday = sunday,
  collectNum = collectNum,
  spareNum = spareNum,
  educateFee = educateFee,
  visitReceiveFlag = visitReceiveFlag,
  onlineReceiveFlag = onlineReceiveFlag,
  url = "https://example.com/class",
)
