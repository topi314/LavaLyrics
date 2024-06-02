package com.github.topi314.lavalyrics.plugin

import com.github.topi314.lavalyrics.LyricsManager
import com.github.topi314.lavalyrics.api.LyricsPluginInfoModifier
import com.github.topi314.lavalyrics.protocol.LyricsFoundEvent
import com.github.topi314.lavalyrics.protocol.LyricsNotFoundEvent
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import dev.arbjerg.lavalink.api.IPlayer
import dev.arbjerg.lavalink.api.ISocketContext
import dev.arbjerg.lavalink.api.PluginEventHandler
import org.springframework.stereotype.Component

@Component
class LavaLyricsPluginEventHandler(
    private val lyricsManager: LyricsManager,
    private val pluginInfoModifiers: List<LyricsPluginInfoModifier>
) : PluginEventHandler() {

    val subscribedPlayers = mutableMapOf<String, MutableSet<Long>>()

    override fun onWebSocketOpen(context: ISocketContext, resumed: Boolean) {
        this.subscribedPlayers[context.sessionId] = mutableSetOf()
    }

    override fun onSocketContextDestroyed(context: ISocketContext) {
        this.subscribedPlayers.remove(context.sessionId)
    }

    override fun onNewPlayer(context: ISocketContext, player: IPlayer) {
        player.audioPlayer.addListener(PlayerListener(this, context, player.guildId))
    }

    override fun onDestroyPlayer(context: ISocketContext, player: IPlayer) {
        subscribedPlayers[player.guildId.toString()]?.remove(player.guildId)
    }

    class PlayerListener(
        private val plugin: LavaLyricsPluginEventHandler,
        private val socketContext: ISocketContext,
        private val guildId: Long,
    ) : AudioEventAdapter() {
        override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
            if (!plugin.subscribedPlayers[socketContext.sessionId]?.contains(guildId)!!) {
                return
            }

            val lyrics = plugin.lyricsManager.loadLyrics(track, false)
            if (lyrics == null || lyrics.lines == null || lyrics.lines!!.isEmpty()) {
                socketContext.sendMessage(LyricsNotFoundEvent.serializer(), LyricsNotFoundEvent(guildId.toString()))
                return
            }

            socketContext.sendMessage(LyricsFoundEvent.serializer(), LyricsFoundEvent(guildId.toString(), lyrics.toLyrics(plugin.pluginInfoModifiers)))

            // Add a marker for the first line
            track.addMarker(TrackMarker(lyrics.lines!![0].timestamp.toMillis(), LineHandler(socketContext, plugin.pluginInfoModifiers, guildId, track, lyrics.lines!!)))
        }

    }
}