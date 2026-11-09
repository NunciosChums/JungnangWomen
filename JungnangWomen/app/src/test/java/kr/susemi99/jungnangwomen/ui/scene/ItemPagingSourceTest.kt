package kr.susemi99.jungnangwomen.ui.scene

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kr.susemi99.jungnangwomen.api.Api
import kr.susemi99.jungnangwomen.api.ApiException
import kr.susemi99.jungnangwomen.apiRespondingWith
import kr.susemi99.jungnangwomen.fixture
import kr.susemi99.jungnangwomen.model.ResultItem
import kr.susemi99.jungnangwomen.model.RowItem
import kr.susemi99.jungnangwomen.model.WomenResourcesClassItem
import kr.susemi99.jungnangwomen.model.WomenResourcesClassParentItem
import kr.susemi99.jungnangwomen.rowItem
import kr.susemi99.jungnangwomen.ui.scene.ItemPagingSource.Companion.PAGE_SIZE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ItemPagingSourceTest {
  private class FakeApi(private val respond: () -> WomenResourcesClassParentItem) : Api {
    val requestedRanges = mutableListOf<IntRange>()

    override suspend fun list(startIndex: Int, endIndex: Int): WomenResourcesClassParentItem {
      requestedRanges += startIndex..endIndex
      return respond()
    }
  }

  private val fullPage = List(PAGE_SIZE) { rowItem(classCode = "C$it") }

  private fun success(totalCount: Int, code: String = "INFO-000", rows: List<RowItem> = fullPage) =
    WomenResourcesClassParentItem(
      classItem = WomenResourcesClassItem(totalCount, ResultItem(code, "메시지"), rows)
    )

  private fun pagerFor(api: Api) = TestPager(PagingConfig(pageSize = PAGE_SIZE), ItemPagingSource(api))

  private fun LoadResult<Int, RowItem>.asPage() = this as LoadResult.Page<Int, RowItem>

  private fun LoadResult<Int, RowItem>.asError() = this as LoadResult.Error<Int, RowItem>

  @Test
  fun `첫 페이지는 1번부터 30번까지 요청한다`() = runBlocking {
    val api = FakeApi { success(totalCount = 90) }

    val page = pagerFor(api).refresh().asPage()

    assertEquals(listOf(1..30), api.requestedRanges)
    assertEquals(fullPage, page.data)
    assertNull(page.prevKey)
    assertEquals(2, page.nextKey)
  }

  @Test
  fun `다음 페이지는 31번부터 60번까지 요청한다`() = runBlocking {
    val api = FakeApi { success(totalCount = 90) }
    val pager = pagerFor(api)

    pager.refresh()
    val page = checkNotNull(pager.append()).asPage()

    assertEquals(listOf(1..30, 31..60), api.requestedRanges)
    assertEquals(1, page.prevKey)
    assertEquals(3, page.nextKey)
  }

  @Test
  fun `전체 개수가 페이지 크기와 같으면 다음 페이지가 없다`() = runBlocking {
    val page = pagerFor(FakeApi { success(totalCount = PAGE_SIZE) }).refresh().asPage()

    assertNull(page.nextKey)
  }

  @Test
  fun `전체 개수가 페이지 크기보다 하나 많으면 다음 페이지가 있다`() = runBlocking {
    val page = pagerFor(FakeApi { success(totalCount = PAGE_SIZE + 1) }).refresh().asPage()

    assertEquals(2, page.nextKey)
  }

  @Test
  fun `실제 결과 없음 응답은 빈 페이지가 된다`() = runBlocking {
    val page = pagerFor(apiRespondingWith(fixture("list_no_data.json"))).refresh().asPage()

    assertTrue(page.data.isEmpty())
    assertNull(page.prevKey)
    assertNull(page.nextKey)
  }

  @Test
  fun `최상위 RESULT의 오류 코드는 ApiException이 된다`() = runBlocking {
    val api = FakeApi { WomenResourcesClassParentItem(result = ResultItem("ERROR-500", "서버 오류")) }

    val error = pagerFor(api).refresh().asError().throwable as ApiException

    assertEquals("ERROR-500", error.code)
    assertEquals("[ERROR-500] 서버 오류", error.message)
  }

  @Test
  fun `classItem의 오류 코드도 ApiException이 된다`() = runBlocking {
    val api = FakeApi { success(totalCount = 0, code = "ERROR-600", rows = emptyList()) }

    val error = pagerFor(api).refresh().asError().throwable as ApiException

    assertEquals("ERROR-600", error.code)
  }

  @Test
  fun `네트워크 예외는 LoadResult Error로 전달한다`() = runBlocking {
    val exception = IOException("네트워크 오류")

    val result = pagerFor(FakeApi { throw exception }).refresh().asError()

    assertSame(exception, result.throwable)
  }

  @Test
  fun `코루틴 취소는 오류로 바꾸지 않고 다시 던진다`() {
    assertThrows(CancellationException::class.java) {
      runBlocking { pagerFor(FakeApi { throw CancellationException("취소") }).refresh() }
    }
  }

  @Test
  fun `실제 XML 오류 응답은 ApiException이 된다`() = runBlocking {
    val result = pagerFor(apiRespondingWith(fixture("list_invalid_key.xml"))).refresh().asError()

    assertEquals("INFO-100", (result.throwable as ApiException).code)
  }

  @Test
  fun `새로고침은 보고 있는 위치와 관계없이 첫 페이지부터 불러온다`() = runBlocking {
    var nextCode = 0
    val source = ItemPagingSource(FakeApi { success(totalCount = 90, rows = List(PAGE_SIZE) { rowItem(classCode = "C${nextCode++}") }) })
    val pager = TestPager(PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false), source)
    pager.refresh()
    pager.append()

    assertNull(source.getRefreshKey(pager.getPagingState(anchorPosition = 10)))
    assertNull(source.getRefreshKey(pager.getPagingState(anchorPosition = 40)))
  }

  @Test
  fun `이미 받은 강좌가 다음 페이지에 다시 오면 제외한다`() = runBlocking {
    var call = 0
    val api = FakeApi {
      // 두 번째 응답의 첫 행은 첫 번째 응답의 마지막 행과 같다.
      val first = (call++) * (PAGE_SIZE - 1)
      success(totalCount = 90, rows = List(PAGE_SIZE) { rowItem(classCode = "C${first + it}") })
    }
    val pager = pagerFor(api)

    pager.refresh()
    val page = checkNotNull(pager.append()).asPage()

    assertEquals(PAGE_SIZE - 1, page.data.size)
    assertEquals("C$PAGE_SIZE", page.data.first().classCode)
    assertEquals(3, page.nextKey)
  }
}
