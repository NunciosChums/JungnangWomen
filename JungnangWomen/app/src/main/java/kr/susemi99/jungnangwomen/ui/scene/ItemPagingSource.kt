package kr.susemi99.jungnangwomen.ui.scene

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import kr.susemi99.jungnangwomen.api.Api
import kr.susemi99.jungnangwomen.api.ApiException
import kr.susemi99.jungnangwomen.model.RowItem

/**
 * 페이지 번호(1, 2, 3…) 기반 PagingSource.
 * 서울 열린데이터광장 API는 인덱스가 1부터 시작하고 양끝을 포함하므로
 * page 번호를 `startIndex = (page - 1) * size + 1`, `endIndex = page * size` 로 변환한다.
 */
class ItemPagingSource constructor(private val api: Api) : PagingSource<Int, RowItem>() {

  // 페이지를 불러오는 사이에 서버 쪽 순서가 바뀌면 같은 강좌가 다시 올 수 있다. LazyColumn 키가 겹치면 앱이 종료되므로 걸러낸다.
  private val seenKeys = mutableSetOf<String>()

  // 새로고침하면 목록 맨 위로 이동하므로 현재 위치와 관계없이 항상 첫 페이지부터 불러온다.
  override fun getRefreshKey(state: PagingState<Int, RowItem>): Int? = null

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RowItem> {
    val page = params.key ?: FIRST_PAGE
    val startIndex = (page - 1) * PAGE_SIZE + 1
    val endIndex = page * PAGE_SIZE

    return try {
      val result = api.list(startIndex = startIndex, endIndex = endIndex)
      // 오류 시 classItem 대신 최상위 RESULT 에 코드가 담겨 온다 (INFO-200 은 단순히 결과 없음).
      val resultItem = result.classItem?.result ?: result.result
      if (resultItem != null && resultItem.code !in SUCCESS_CODES) {
        return LoadResult.Error(ApiException(resultItem.code, resultItem.message))
      }
      val totalCount = result.classItem?.listTotalCount ?: 0
      LoadResult.Page(
        data = result.classItem?.rows.orEmpty().filter { seenKeys.add(it.key) },
        prevKey = if (page == FIRST_PAGE) null else page - 1,
        nextKey = if (endIndex < totalCount) page + 1 else null
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      LoadResult.Error(e)
    }
  }

  companion object {
    const val FIRST_PAGE = 1
    const val PAGE_SIZE = 30
    private val SUCCESS_CODES = setOf("INFO-000", "INFO-200")
  }
}
