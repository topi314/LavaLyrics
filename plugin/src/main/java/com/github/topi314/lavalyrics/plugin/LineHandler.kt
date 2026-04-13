package com.github.topi314.lavalyrics.plugin

import com.github.topi314.lavalyrics.api.LyricsPluginInfoModifier
import com.github.topi314.lavalyrics.lyrics.AudioLyrics
import com.github.topi314.lavalyrics.protocol.LyricsLineEvent
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import com.sedmelluq.discord.lavaplayer.track.TrackMarkerHandler
import com.sedmelluq.discord.lavaplayer.track.TrackMarkerHandler.MarkerState
import dev.arbjerg.lavalink.api.ISocketContext
import kotlin.concurrent.fixedRateTimer
import java.util.Timer

class LineHandler(
    private val context: ISocketContext,
    private val pluginInfoModifiers: List<LyricsPluginInfoModifier>,
    private val guildId: Long,
    private val track: AudioTrack,
    private val lines: List<AudioLyrics.Line>,
) : TrackMarkerHandler {
    private var currentLine = 0
    
    @Volatile private var lastPositionMs: Long = 0

    private val positionTracker: Timer = fixedRateTimer(period = 250L, daemon = true) {
        val position = track.position
        if (position < lastPositionMs - 500L) resync(position)
        lastPositionMs = position
    }

    @Synchronized
    override fun handle(state: MarkerState) {
        if (state == MarkerState.STOPPED || state == MarkerState.ENDED || state == MarkerState.REMOVED) {
            positionTracker.cancel()
            return
        }
        if (!(state == MarkerState.REACHED || state == MarkerState.LATE || state == MarkerState.BYPASSED)) {
            return
        }

        val skipped = state == MarkerState.BYPASSED || state == MarkerState.LATE
        context.sendMessage(LyricsLineEvent.serializer(), LyricsLineEvent(guildId.toString(), currentLine, lines[currentLine].toLine(pluginInfoModifiers), skipped))

        currentLine++
        if (this.currentLine < lines.size) {
            track.addMarker(TrackMarker(lines[currentLine].timestamp.toMillis(), this))
        } else {
            positionTracker.cancel()
        }
    }

    @Synchronized
    private fun resync(positionMs: Long) {
        val targetLine = lines.indexOfLast { it.timestamp.toMillis() <= positionMs }.coerceAtLeast(0)
        currentLine = targetLine
        track.setMarker(TrackMarker(lines[currentLine].timestamp.toMillis(), this))
    }
}