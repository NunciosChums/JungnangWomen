package kr.susemi99.jungnangwomen.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kr.susemi99.jungnangwomen.util.serializer.SkipInvalidRowListSerializer

@Serializable
data class WomenResourcesClassItem(
  @SerialName("list_total_count") val listTotalCount: Int,
  @SerialName("RESULT") val result: ResultItem,
  @Serializable(with = SkipInvalidRowListSerializer::class)
  @SerialName("row") val rows: List<RowItem>
)
