package com.github.topi314.lavalyrics.api

import com.github.topi314.lavalyrics.lyrics.AudioLyrics
import kotlinx.serialization.json.JsonObject

interface LyricsPluginInfoModifier {

    fun modifyAudioLyricsPluginInfo(text: AudioLyrics): JsonObject? = null

    fun modifyAudioLyricsLinePluginInfo(result: AudioLyrics.Line): JsonObject? = null

}
