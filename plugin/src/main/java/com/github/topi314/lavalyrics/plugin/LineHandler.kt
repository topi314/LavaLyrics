package com.github.topi314.lavalyrics.plugin

import com.github.topi314.lavalyrics.api.LyricsPluginInfoModifier
import com.github.topi314.lavalyrics.lyrics.AudioLyrics
import com.github.topi314.lavalyrics.protocol.LyricsLineEvent
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import com.sedmelluq.discord.lavaplayer.track.TrackMarkerHandler
import dev.arbjerg.lavalink.api.ISocketContext

class LineHandler(
    private val context: ISocketContext,
    private val pluginInfoModifiers: List<LyricsPluginInfoModifier>,
    private val guildId: Long,
    private val track: AudioTrack,
    private val lines: List<AudioLyrics.Line>,
) : TrackMarkerHandler {
    private var currentLine = 0

    override fun handle(state: TrackMarkerHandler.MarkerState) {
        if (!(state == TrackMarkerHandler.MarkerState.REACHED || state == TrackMarkerHandler.MarkerState.LATE || state == TrackMarkerHandler.MarkerState.BYPASSED)) {
            return
        }

        val line = lines[currentLine]
        context.sendMessage(LyricsLineEvent.serializer(), LyricsLineEvent(guildId.toString(), line.toLine(pluginInfoModifiers)))

        currentLine++
        if (this.currentLine < lines.size) {
            track.addMarker(TrackMarker(lines[currentLine].timestamp.toMillis(), this))
        }
    }
}
