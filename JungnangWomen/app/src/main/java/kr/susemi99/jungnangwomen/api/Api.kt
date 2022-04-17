package kr.susemi99.jungnangwomen.api

import kr.susemi99.jungnangwomen.model.WomenResourcesClassParentItem
import retrofit2.http.GET
import retrofit2.http.Path

interface Api {
  @GET("{start_index}/{end_index}")
  suspend fun list(@Path("start_index") startIndex: Int, @Path("end_index") endIndex: Int): WomenResourcesClassParentItem
}