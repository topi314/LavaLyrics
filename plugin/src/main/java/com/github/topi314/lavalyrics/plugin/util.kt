package com.github.topi314.lavalyrics.plugin

import com.github.topi314.lavalyrics.api.LyricsPluginInfoModifier
import com.github.topi314.lavalyrics.lyrics.AudioLyrics
import com.github.topi314.lavalyrics.protocol.Line
import com.github.topi314.lavalyrics.protocol.Lyrics
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.time.toKotlinDuration

fun AudioLyrics.toLyrics(lyricsInfoModifiers: List<LyricsPluginInfoModifier>): Lyrics {

    val plugin = lyricsInfoModifiers.fold(JsonObject(emptyMap())) { acc, it ->
        val jsonObject = it.modifyAudioLyricsPluginInfo(this) ?: JsonObject(emptyMap())
        acc + jsonObject
    }

    return Lyrics(sourceName, originalSourceName, text, lines?.map { it.toLine(lyricsInfoModifiers) }, plugin)
}

fun AudioLyrics.Line.toLine(lyricsInfoModifiers: List<LyricsPluginInfoModifier>): Line {
    val plugin = lyricsInfoModifiers.fold(JsonObject(emptyMap())) { acc, it ->
        val jsonObject = it.modifyAudioLyricsLinePluginInfo(this) ?: JsonObject(emptyMap())
        acc + jsonObject
    }

    return Line(timestamp.toKotlinDuration(), duration?.toKotlinDuration(), line, plugin)
}

private operator fun JsonObject.plus(other: JsonObject) = JsonObject(this + (other as Map<String, JsonElement>))
