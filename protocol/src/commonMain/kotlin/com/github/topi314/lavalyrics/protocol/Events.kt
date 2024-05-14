package com.github.topi314.lavalyrics.protocol

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class LyricsFoundEvent(
    val guildId: String,
    val lyrics: Lyrics,
) {
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val op: String = "event"

    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val type: String = "LyricsFoundEvent"
}

@Serializable
data class LyricsNotFoundEvent(
    val guildId: String,
) {
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val op: String = "event"

    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val type: String = "LyricsNotFoundEvent"
}

@Serializable
data class LyricsLineEvent(
    val guildId: String,
    val line: Line
) {
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val op: String = "event"

    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val type: String = "LyricsLineEvent"
}
