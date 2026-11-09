package kr.susemi99.jungnangwomen.ui.scene

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.flowOf
import kr.susemi99.jungnangwomen.R
import kr.susemi99.jungnangwomen.api.ApiException
import kr.susemi99.jungnangwomen.model.RowItem
import kr.susemi99.jungnangwomen.ui.theme.JungnangWomenTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

/** 네트워크 없이 목록 화면의 상태별 표시를 확인한다. */
@RunWith(AndroidJUnit4::class)
class MainContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  private val context = InstrumentationRegistry.getInstrumentation().targetContext
  private val notLoading = LoadState.NotLoading(endOfPaginationReached = true)

  private fun show(data: PagingData<RowItem>) {
    val flow = flowOf(data)
    composeRule.setContent {
      JungnangWomenTheme {
        MainContent(listItems = flow.collectAsLazyPagingItems())
      }
    }
  }

  @Test
  fun 목록_항목의_내용을_표시한다() {
    show(PagingData.from(listOf(row)))

    composeRule.onNodeWithText("[초급] 테스트 강좌").assertIsDisplayed()
    composeRule.onNodeWithText("신청기간: ").assertIsDisplayed()
    composeRule.onNodeWithText("5/20명").assertIsDisplayed()
    composeRule.onNodeWithText("100,000원").assertIsDisplayed()
    composeRule.onNodeWithText("방문, 온라인").assertIsDisplayed()
  }

  @Test
  fun 결과가_없으면_안내_문구를_표시한다() {
    show(PagingData.empty(LoadStates(refresh = notLoading, prepend = notLoading, append = notLoading)))

    composeRule.onNodeWithText(context.getString(R.string.no_result)).assertIsDisplayed()
  }

  @Test
  fun 불러오기에_실패하면_오류와_재시도_버튼을_표시한다() {
    val error = LoadState.Error(ApiException("ERROR-500", "서버 오류"))

    show(PagingData.empty(LoadStates(refresh = error, prepend = notLoading, append = notLoading)))

    composeRule.onNodeWithText(context.getString(R.string.load_error)).assertIsDisplayed()
    composeRule.onNodeWithText("[ERROR-500] 서버 오류").assertIsDisplayed()
    composeRule.onNodeWithText(context.getString(R.string.retry)).assertIsDisplayed()
  }

  @Test
  fun 재시도_버튼을_누르면_콜백을_호출한다() {
    var retryCount = 0
    composeRule.setContent {
      JungnangWomenTheme {
        ErrorView(message = "오류", onRetry = { retryCount++ })
      }
    }

    composeRule.onNodeWithText(context.getString(R.string.retry)).performClick()

    composeRule.runOnIdle { assertEquals(1, retryCount) }
  }

  private val row = RowItem(
    classCode = "C1",
    className = "테스트 강좌",
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
    url = "https://example.com/class",
  )
}
