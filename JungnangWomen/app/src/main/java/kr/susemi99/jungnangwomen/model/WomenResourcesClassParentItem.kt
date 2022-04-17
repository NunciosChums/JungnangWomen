package kr.susemi99.jungnangwomen.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WomenResourcesClassParentItem(
  @SerialName("SeoulJungNangWomenResourcesClass") val classItem: WomenResourcesClassItem? = null,
  @SerialName("RESULT") val result: ResultItem? = null
)
