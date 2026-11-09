package kr.susemi99.jungnangwomen.util.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kr.susemi99.jungnangwomen.model.RowItem
import java.time.format.DateTimeParseException

/**
 * 형식이 잘못된 행(날짜·시간이 빈 값 등)만 건너뛰고 나머지 행은 그대로 읽는다.
 * 행 하나의 오류로 페이지 전체가 실패하지 않게 하기 위한 것이다.
 */
object SkipInvalidRowListSerializer : KSerializer<List<RowItem>> {
  private val delegate = ListSerializer(RowItem.serializer())

  override val descriptor: SerialDescriptor = delegate.descriptor

  override fun serialize(encoder: Encoder, value: List<RowItem>) = delegate.serialize(encoder, value)

  override fun deserialize(decoder: Decoder): List<RowItem> {
    val jsonDecoder = decoder as JsonDecoder
    return jsonDecoder.decodeJsonElement().jsonArray.mapNotNull { element ->
      decodeOrNull(jsonDecoder, element)
    }
  }

  private fun decodeOrNull(decoder: JsonDecoder, element: JsonElement): RowItem? =
    try {
      decoder.json.decodeFromJsonElement(RowItem.serializer(), element)
    } catch (_: IllegalArgumentException) { // SerializationException 포함
      null
    } catch (_: DateTimeParseException) {
      null
    }
}
