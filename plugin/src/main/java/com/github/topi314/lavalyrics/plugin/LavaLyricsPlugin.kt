package com.github.topi314.lavalyrics.plugin

import com.github.topi314.lavalyrics.LyricsManager
import com.github.topi314.lavalyrics.api.LyricsPluginInfoModifier
import com.github.topi314.lavalyrics.protocol.Lyrics
import com.github.topi314.lavalyrics.protocol.LyricsFoundEvent
import com.github.topi314.lavalyrics.protocol.LyricsNotFoundEvent
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import dev.arbjerg.lavalink.api.ISocketContext
import dev.arbjerg.lavalink.api.ISocketServer
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.io.ByteArrayInputStream
import java.util.*

@RestController
class LavaLyricsPlugin(
    private val lavaLyricsPluginEventHandler: LavaLyricsPluginEventHandler,
    private val audioPlayerManager: AudioPlayerManager,
    private val socketServer: ISocketServer,
    private val lyricsManager: LyricsManager,
    private val pluginInfoModifiers: List<LyricsPluginInfoModifier>
) {

    companion object {
        val log = LoggerFactory.getLogger(LavaLyricsPlugin::class.java)

        fun socketContext(socketServer: ISocketServer, sessionId: String) =
            socketServer.sessions[sessionId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found")

        fun existingPlayer(socketContext: ISocketContext, guildId: Long) =
            socketContext.players[guildId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found")

        fun decodeTrack(audioPlayerManager: AudioPlayerManager, message: String): AudioTrack {
            val bais = ByteArrayInputStream(Base64.getDecoder().decode(message))
            return audioPlayerManager.decodeTrack(MessageInput(bais)).decodedTrack
                ?: throw IllegalStateException("Failed to decode track due to a mismatching version or missing source manager")
        }
    }

    @PostMapping("/v4/sessions/{sessionId}/players/{guildId}/lyrics/subscribe")
    fun subscribeToLyrics(
        request: HttpServletRequest,
        @PathVariable sessionId: String,
        @PathVariable guildId: String
    ): ResponseEntity<Void> {
        log.debug("subscribeToLyrics called with sessionId: {}, guildId: {}", sessionId, guildId)
        if (lavaLyricsPluginEventHandler.subscribedPlayers[sessionId]?.add(guildId.toLong()) == false) {
            return ResponseEntity.noContent().build()
        }
        val socketContext = socketContext(socketServer, sessionId)
        val player = existingPlayer(socketContext, guildId.toLong())
        if (player.track == null) {
            return ResponseEntity.noContent().build()
        }

        val lyrics = lyricsManager.loadLyrics(player.track, false)
        if (lyrics == null || lyrics.lines == null || lyrics.lines!!.isEmpty()) {
            socketContext.sendMessage(LyricsNotFoundEvent.serializer(), LyricsNotFoundEvent(guildId))
            return ResponseEntity.noContent().build()
        }

        socketContext.sendMessage(LyricsFoundEvent.serializer(), LyricsFoundEvent(guildId, lyrics.toLyrics(pluginInfoModifiers)))

        // Add a marker for the first line
        player.track?.addMarker(TrackMarker(lyrics.lines!![0].timestamp.toMillis(), LineHandler(socketContext, pluginInfoModifiers, guildId.toLong(), player.track!!, lyrics.lines!!)))
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/v4/sessions/{sessionId}/players/{guildId}/unsubscribe")
    fun unsubscribeFromLyrics(
        request: HttpServletRequest,
        @PathVariable sessionId: String,
        @PathVariable guildId: String
    ): ResponseEntity<Void> {
        log.debug("unsubscribeFromLyrics called with sessionId: {}, guildId: {}", sessionId, guildId)
        lavaLyricsPluginEventHandler.subscribedPlayers[sessionId]?.remove(guildId.toLong())
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/v4/sessions/{sessionId}/players/{guildId}/track/lyrics")
    fun loadCurrentTrackLyrics(
        request: HttpServletRequest,
        @PathVariable sessionId: String,
        @PathVariable guildId: String,
        @RequestParam skipTrackSource: Boolean = false
    ): ResponseEntity<Lyrics> {
        log.debug("getCurrentTrackLyrics called with sessionId: {}, guildId: {}, skipTrackSource: {}", sessionId, guildId, skipTrackSource)
        val socketContext = socketContext(socketServer, sessionId)
        val player = existingPlayer(socketContext, guildId.toLong())
        if (player.track == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No track playing")
        }

        val lyrics = lyricsManager.loadLyrics(player.track, skipTrackSource)
        return if (lyrics != null) {
            ResponseEntity.ok(lyrics.toLyrics(pluginInfoModifiers))
        } else ResponseEntity.noContent().build()
    }

    @GetMapping("/v4/lyrics")
    fun loadLyrics(
        request: HttpServletRequest,
        @RequestParam track: String,
        @RequestParam skipTrackSource: Boolean = false
    ): ResponseEntity<Lyrics> {
        log.debug("loadLyrics called with track: {}, skipTrackSource: {}", track, skipTrackSource)
        val decodedTrack = decodeTrack(audioPlayerManager, track)
        val lyrics = lyricsManager.loadLyrics(decodedTrack, skipTrackSource)

        return if (lyrics != null) {
            ResponseEntity.ok(lyrics.toLyrics(pluginInfoModifiers))
        } else ResponseEntity.noContent().build()
    }


}