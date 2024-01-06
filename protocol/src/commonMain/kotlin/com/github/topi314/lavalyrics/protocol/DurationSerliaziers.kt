package com.github.topi314.lavalyrics.protocol

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

// -------- as Long --------

/** Serializer that encodes and decodes [Duration]s as a [Long] number of the specified [unit]. */
sealed class DurationAsLongSerializer(
    private val unit: DurationUnit,
    private val name: String,
) : KSerializer<Duration> {

    final override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.github.topi314.lavalyrics.protocol.$name", PrimitiveKind.LONG)

    final override fun serialize(encoder: Encoder, value: Duration) {
        when (val valueAsLong = value.toLong(unit)) {

            Long.MIN_VALUE, Long.MAX_VALUE -> throw SerializationException(
                if (value.isInfinite()) {
                    "Infinite Durations cannot be serialized, got $value"
                } else {
                    "The Duration $value expressed as a number of ${
                        unit.name.lowercase()
                    } does not fit in the range of Long type and therefore cannot be serialized with ${name}Serializer"
                }
            )

            else -> encoder.encodeLong(valueAsLong)
        }
    }

    final override fun deserialize(decoder: Decoder): Duration {
        return decoder.decodeLong().toDuration(unit)
    }
}


// milliseconds

/** Serializer that encodes and decodes [Duration]s in [whole milliseconds][Duration.inWholeMilliseconds]. */
object DurationInMillisecondsSerializer : DurationAsLongSerializer(MILLISECONDS, "DurationInMilliseconds")

/** A [Duration] that is [serializable][Serializable] with [DurationInMillisecondsSerializer]. */
typealias DurationInMilliseconds = @Serializable(with = DurationInMillisecondsSerializer::class) Duration
