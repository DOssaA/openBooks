package com.darioossa.openbooks.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class WorkDto(
    @SerialName("key") val key: String,
    @SerialName("title") val title: String,
    @SerialName("description")
    @Serializable(with = DescriptionSerializer::class)
    val description: String? = null,
    @SerialName("covers") val covers: List<Int>? = null,
)

/**
 * OpenLibrary serves a Work's `description` as either a plain JSON string or a
 * `{ "type": ..., "value": "..." }` object. This serializer reads both shapes and yields the text.
 */
object DescriptionSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Description", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String? {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeString()
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> if (element.isString) element.content else null
            is JsonObject -> element["value"]?.jsonPrimitive?.content
            else -> null
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: String?,
    ) {
        encoder.encodeString(value.orEmpty())
    }
}
