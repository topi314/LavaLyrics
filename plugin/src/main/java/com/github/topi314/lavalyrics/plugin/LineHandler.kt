package com.github.topi314.lavalyrics.plugin

import com.github.topi314.lavalyrics.api.LyricsPluginInfoModifier
import com.github.topi314.lavalyrics.lyrics.AudioLyrics
import com.github.topi314.lavalyrics.protocol.LyricsLineEvent
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import com.sedmelluq.discord.lavaplayer.track.TrackMarkerHandler
import com.sedmelluq.discord.lavaplayer.track.TrackMarkerHandler.MarkerState
import dev.arbjerg.lavalink.api.ISocketContext

class LineHandler(
    private val context: ISocketContext,
    private val pluginInfoModifiers: List<LyricsPluginInfoModifier>,
    private val guildId: Long,
    private val track: AudioTrack,
    private val lines: List<AudioLyrics.Line>,
) : TrackMarkerHandler {
    private var currentLine = 0

    override fun handle(state: MarkerState) {
        if (!(state == MarkerState.REACHED || state == MarkerState.LATE || state == MarkerState.BYPASSED)) {
            return
        }

        val skipped = state == MarkerState.BYPASSED || state == MarkerState.LATE
        context.sendMessage(LyricsLineEvent.serializer(), LyricsLineEvent(guildId.toString(), currentLine, lines[currentLine].toLine(pluginInfoModifiers), skipped))

        currentLine++
        if (this.currentLine < lines.size) {
            track.addMarker(TrackMarker(lines[currentLine].timestamp.toMillis(), this))
        }
    }
}
