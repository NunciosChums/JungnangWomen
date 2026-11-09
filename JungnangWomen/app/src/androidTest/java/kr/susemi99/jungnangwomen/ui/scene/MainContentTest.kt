package kr.susemi99.jungnangwomen.ui.scene

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
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
import java.net.UnknownHostException

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
  fun 불러오는_중에는_로딩_표시를_보여주고_결과_없음은_표시하지_않는다() {
    show(PagingData.empty(LoadStates(refresh = LoadState.Loading, prepend = notLoading, append = notLoading)))

    composeRule.onNodeWithContentDescription(context.getString(R.string.loading)).assertIsDisplayed()
    composeRule.onNodeWithText(context.getString(R.string.no_result)).assertDoesNotExist()
  }

  @Test
  fun 불러오기가_끝나면_로딩_표시를_없앤다() {
    show(PagingData.from(listOf(row), LoadStates(refresh = notLoading, prepend = notLoading, append = notLoading)))

    // LazyPagingItems의 초기 refresh 상태가 Loading이므로 데이터가 표시될 때까지 기다린다.
    composeRule.waitUntil(timeoutMillis = 5_000) {
      composeRule.onAllNodesWithText("[초급] 테스트 강좌").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
    }
    composeRule.onNodeWithContentDescription(context.getString(R.string.loading)).assertDoesNotExist()
  }

  @Test
  fun 네트워크_오류는_원문_대신_안내_문구를_표시한다() {
    val error = LoadState.Error(UnknownHostException("Unable to resolve host"))

    show(PagingData.empty(LoadStates(refresh = error, prepend = notLoading, append = notLoading)))

    composeRule.onNodeWithText(context.getString(R.string.network_error)).assertIsDisplayed()
    composeRule.onNodeWithText("Unable to resolve host").assertDoesNotExist()
  }

  @Test
  fun 그_외_오류는_원문_대신_알_수_없는_오류를_표시한다() {
    val error = LoadState.Error(IllegalStateException("Unexpected JSON token"))

    show(PagingData.empty(LoadStates(refresh = error, prepend = notLoading, append = notLoading)))

    composeRule.onNodeWithText(context.getString(R.string.unknown_error)).assertIsDisplayed()
    composeRule.onNodeWithText("Unexpected JSON token").assertDoesNotExist()
  }

  @Test
  fun 열_수_없는_링크를_눌러도_앱이_종료되지_않는다() {
    show(PagingData.from(listOf(row.copy(url = ""))))

    composeRule.onNodeWithText("[초급] 테스트 강좌").performClick()

    composeRule.onNodeWithText("[초급] 테스트 강좌").assertIsDisplayed()
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

  private val row = testRow()
}
