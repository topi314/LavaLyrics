package com.github.topi314.lavalyrics.plugin

import com.github.topi314.lavalyrics.api.LyricsPluginInfoModifier
import com.github.topi314.lavalyrics.lyrics.AudioLyrics
import com.github.topi314.lavalyrics.protocol.LyricsLineEvent
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState
import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import com.sedmelluq.discord.lavaplayer.track.TrackMarkerHandler
import com.sedmelluq.discord.lavaplayer.track.TrackMarkerHandler.MarkerState
import dev.arbjerg.lavalink.api.ISocketContext
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LineHandler(
    private val context: ISocketContext,
    private val pluginInfoModifiers: List<LyricsPluginInfoModifier>,
    private val guildId: Long,
    private val track: AudioTrack,
    private val lines: List<AudioLyrics.Line>,
) : TrackMarkerHandler {
    companion object {
        private const val RETRY_DELAY_MS = 50L
        private val scheduler = Executors.newSingleThreadScheduledExecutor { Thread(it, "lavalyrics-seek").apply { isDaemon = true } }
    }

    private var currentLine = 0
    private var marker: TrackMarker? = null
    private var seeks = 0
    private var addingAfterSeek = false
    private var lateAfterSeek = false

    fun start() {
        addMarker()
    }

    @Synchronized
    override fun handle(state: MarkerState) {
        if (!(state == MarkerState.REACHED || state == MarkerState.LATE || state == MarkerState.BYPASSED)) {
            return
        }

        if (state == MarkerState.LATE && addingAfterSeek) {
            lateAfterSeek = true
            return
        }

        val skipped = state == MarkerState.BYPASSED || state == MarkerState.LATE
        sendLine(skipped)

        currentLine++
        addMarker()
    }

    // lavaplayer only triggers markers that a seek jumps over, so after seeking backwards the next marker has to be moved back
    @Synchronized
    fun onSeek(position: Long) {
        if (currentLine == 0 || position >= lines[currentLine - 1].timestamp.toMillis()) {
            return
        }

        seeks++
        marker?.let { track.removeMarker(it) }
        marker = null
        currentLine = lines.indexOfLast { it.timestamp.toMillis() <= position }
        if (currentLine == -1) {
            currentLine = 0
        } else {
            sendLine(true)
            currentLine++
        }
        addMarkerAfterSeek(seeks)
    }

    // Right after a seek the track can still report the position from before the seek until the first frame
    // from the new position is played. A marker added then is treated as already passed, so it is added again
    // once the track reports the new position.
    private fun addMarkerAfterSeek(seek: Int) {
        val reportedPosition = track.position
        lateAfterSeek = false
        addingAfterSeek = true
        try {
            addMarker()
        } finally {
            addingAfterSeek = false
        }

        if (lateAfterSeek) {
            marker = null
            scheduler.schedule({ retryMarker(seek, reportedPosition) }, RETRY_DELAY_MS, TimeUnit.MILLISECONDS)
        }
    }

    @Synchronized
    private fun retryMarker(seek: Int, reportedPosition: Long) {
        if (seek != seeks || track.state == AudioTrackState.FINISHED) {
            return
        }

        // with seek ghosting, frames from before the seek keep playing (and moving the position forward)
        // until the first frame from the new position, which is before the reported position
        if (track.position >= reportedPosition) {
            scheduler.schedule({ retryMarker(seek, reportedPosition) }, RETRY_DELAY_MS, TimeUnit.MILLISECONDS)
            return
        }
        addMarker()
    }

    private fun sendLine(skipped: Boolean) {
        context.sendMessage(LyricsLineEvent.serializer(), LyricsLineEvent(guildId.toString(), currentLine, lines[currentLine].toLine(pluginInfoModifiers), skipped))
    }

    private fun addMarker() {
        if (currentLine >= lines.size) {
            marker = null
            return
        }

        // set before adding, addMarker calls handle right away when the line is already behind the track position
        val next = TrackMarker(lines[currentLine].timestamp.toMillis(), this)
        marker = next
        track.addMarker(next)
    }
}
