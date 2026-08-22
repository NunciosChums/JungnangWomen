package kr.susemi99.jungnangwomen.ui.scene

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kr.susemi99.jungnangwomen.api.Api
import kr.susemi99.jungnangwomen.model.RowItem

/**
 * 페이지 번호(1, 2, 3…) 기반 PagingSource.
 * 서울 열린데이터광장 API는 인덱스가 1부터 시작하고 양끝을 포함하므로
 * page 번호를 `startIndex = (page - 1) * size + 1`, `endIndex = page * size` 로 변환한다.
 */
class ItemPagingSource constructor(private val api: Api) : PagingSource<Int, RowItem>() {

  override fun getRefreshKey(state: PagingState<Int, RowItem>): Int? {
    return state.anchorPosition?.let { anchorPosition ->
      val anchorPage = state.closestPageToPosition(anchorPosition)
      anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }
  }

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RowItem> {
    val page = params.key ?: FIRST_PAGE
    val startIndex = (page - 1) * PAGE_SIZE + 1
    val endIndex = page * PAGE_SIZE

    return try {
      val result = api.list(startIndex = startIndex, endIndex = endIndex)
      val totalCount = result.classItem?.listTotalCount ?: 0
      LoadResult.Page(
        data = result.classItem?.rows ?: listOf(),
        prevKey = if (page == FIRST_PAGE) null else page - 1,
        nextKey = if (endIndex < totalCount) page + 1 else null
      )
    } catch (e: Exception) {
      LoadResult.Error(e)
    }
  }

  companion object {
    const val FIRST_PAGE = 1
    const val PAGE_SIZE = 30
  }
}
