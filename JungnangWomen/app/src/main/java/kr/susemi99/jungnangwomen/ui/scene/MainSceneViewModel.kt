package kr.susemi99.jungnangwomen.ui.scene

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.susemi99.jungnangwomen.api.Api
import javax.inject.Inject

@HiltViewModel
class MainSceneViewModel @Inject constructor(
  private val api: Api
) : ViewModel() {
  val list = Pager(PagingConfig(pageSize = 3)) { ItemPagingSource(api) }.flow.cachedIn(viewModelScope)
}