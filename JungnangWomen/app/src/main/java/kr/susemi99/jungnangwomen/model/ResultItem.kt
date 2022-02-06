package kr.susemi99.jungnangwomen.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResultItem(
  @SerialName("CODE") val code: String,
  @SerialName("MESSAGE") val message: String
)
