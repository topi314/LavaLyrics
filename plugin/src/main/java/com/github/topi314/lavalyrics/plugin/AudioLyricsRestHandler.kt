package com.github.topi314.lavalyrics.plugin

import com.github.topi314.lavalyrics.LyricsManager
import com.github.topi314.lavalyrics.api.LyricsPluginInfoModifier
import com.github.topi314.lavalyrics.protocol.Lyrics
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.lavalink.api.ISocketContext
import dev.arbjerg.lavalink.api.ISocketServer
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.io.ByteArrayInputStream
import java.util.*

@RestController
class AudioLyricsRestHandler(
    private val audioPlayerManager: AudioPlayerManager,
    private val socketServer: ISocketServer,
    private val lyricsManager: LyricsManager,
    private val pluginInfoModifiers: List<LyricsPluginInfoModifier>
) {

    companion object {
        private val log = LoggerFactory.getLogger(AudioLyricsRestHandler::class.java)
    }

    private fun socketContext(socketServer: ISocketServer, sessionId: String) =
        socketServer.sessions[sessionId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found")

    private fun existingPlayer(socketContext: ISocketContext, guildId: Long) =
        socketContext.players[guildId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found")

    private fun decodeTrack(audioPlayerManager: AudioPlayerManager, message: String): AudioTrack {
        val bais = ByteArrayInputStream(Base64.getDecoder().decode(message))
        return audioPlayerManager.decodeTrack(MessageInput(bais)).decodedTrack
            ?: throw IllegalStateException("Failed to decode track due to a mismatching version or missing source manager")
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
