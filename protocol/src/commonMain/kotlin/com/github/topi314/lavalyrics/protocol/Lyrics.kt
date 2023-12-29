package com.github.topi314.lavalyrics.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmField

@Serializable
data class Lyrics(
    val sourceName: String,
    val text: String,
    val lines: List<Line>,
    val plugin: JsonObject
) {
    companion object {
        @JvmField
        val EMPTY = Lyrics("", "", emptyList(), JsonObject(emptyMap()))
    }
}


@Serializable
data class Line(val timestamp: DurationInMilliseconds, val duration: DurationInMilliseconds, val line: String, val plugin: JsonObject)
