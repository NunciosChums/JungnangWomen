package kr.susemi99.jungnangwomen.ui.scene

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.susemi99.jungnangwomen.R
import kr.susemi99.jungnangwomen.api.Api
import kr.susemi99.jungnangwomen.model.ResultItem
import kr.susemi99.jungnangwomen.model.RowItem
import kr.susemi99.jungnangwomen.model.WomenResourcesClassItem
import kr.susemi99.jungnangwomen.model.WomenResourcesClassParentItem
import kr.susemi99.jungnangwomen.ui.scene.ItemPagingSource.Companion.PAGE_SIZE
import kr.susemi99.jungnangwomen.ui.theme.JungnangWomenTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/** 2페이지 이상을 돌려주는 페이크 API로 실제 [ItemPagingSource]와 목록 화면의 동작을 확인한다. */
@RunWith(AndroidJUnit4::class)
class MainContentPagingTest {
  @get:Rule
  val composeRule = createComposeRule()

  private val context = InstrumentationRegistry.getInstrumentation().targetContext
  private lateinit var items: LazyPagingItems<RowItem>

  private class FakeApi(private val totalCount: Int = PAGE_SIZE * 3) : Api {
    var failAtStartIndex: Int? = null

    override suspend fun list(startIndex: Int, endIndex: Int): WomenResourcesClassParentItem {
      if (startIndex == failAtStartIndex) throw IOException("연결 실패")
      val rows = (startIndex..minOf(endIndex, totalCount)).map { testRow(classCode = "C$it", className = "강좌 $it") }
      return WomenResourcesClassParentItem(classItem = WomenResourcesClassItem(totalCount, ResultItem("INFO-000", "정상"), rows))
    }
  }

  private fun show(api: Api) {
    val flow = Pager(PagingConfig(pageSize = PAGE_SIZE)) { ItemPagingSource(api) }.flow
    composeRule.setContent {
      JungnangWomenTheme {
        items = flow.collectAsLazyPagingItems()
        MainContent(listItems = items)
      }
    }
  }

  private fun waitUntilItemCount(count: Int) = composeRule.waitUntil(timeoutMillis = 5_000) {
    ::items.isInitialized && items.itemCount >= count
  }

  private fun waitUntilTextExists(text: String) = composeRule.waitUntil(timeoutMillis = 5_000) {
    composeRule.onAllNodesWithText(text).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
  }

  private fun scrollTo(index: Int) = composeRule.onNode(hasScrollAction()).performScrollToIndex(index)

  @Test
  fun 깊이_스크롤한_뒤_새로고침하면_맨_위부터_다시_표시한다() {
    show(FakeApi())
    waitUntilItemCount(PAGE_SIZE)
    scrollTo(PAGE_SIZE - 1)
    waitUntilItemCount(PAGE_SIZE * 2)
    scrollTo(PAGE_SIZE * 2 - 1)
    waitUntilItemCount(PAGE_SIZE * 3)
    scrollTo(PAGE_SIZE * 3 - 1)
    waitUntilTextExists("[초급] 강좌 90")

    composeRule.onNodeWithContentDescription(context.getString(R.string.refresh)).performClick()

    waitUntilTextExists("[초급] 강좌 1")
    composeRule.onNodeWithText("[초급] 강좌 1").assertIsDisplayed()
  }

  @Test
  fun 다음_페이지를_불러오지_못하면_목록_하단에_오류를_표시하고_재시도할_수_있다() {
    val api = FakeApi().apply { failAtStartIndex = PAGE_SIZE + 1 }
    show(api)
    waitUntilItemCount(PAGE_SIZE)
    composeRule.waitUntil(timeoutMillis = 5_000) { items.loadState.append is LoadState.Error }

    scrollTo(PAGE_SIZE)
    composeRule.onNodeWithText(context.getString(R.string.network_error)).assertIsDisplayed()

    api.failAtStartIndex = null
    composeRule.onNodeWithText(context.getString(R.string.retry)).performClick()

    waitUntilItemCount(PAGE_SIZE * 2)
    composeRule.onNodeWithText(context.getString(R.string.retry)).assertDoesNotExist()
  }
}
