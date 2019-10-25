package kr.susemi99.jungnangwomen.model

import com.google.gson.annotations.SerializedName

data class WomenResourcesClassParentItem(
  @SerializedName("SeoulJungNangWomenResourcesClass") val classItem: WomenResourcesClassItem,
  @SerializedName("RESULT") val result: ResultItem?
)
