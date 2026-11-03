package com.github.topi314.lavalyrics.plugin

import com.github.topi314.lavalyrics.LyricsManager
import com.github.topi314.lavalyrics.api.LyricsPluginInfoModifier
import com.github.topi314.lavalyrics.lyrics.AudioLyrics
import com.github.topi314.lavalyrics.protocol.LyricsFoundEvent
import com.github.topi314.lavalyrics.protocol.LyricsNotFoundEvent
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import dev.arbjerg.lavalink.api.IPlayer
import dev.arbjerg.lavalink.api.ISocketContext
import dev.arbjerg.lavalink.api.PluginEventHandler
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class LavaLyricsPluginEventHandler(
    private val lyricsManager: LyricsManager,
    private val pluginInfoModifiers: List<LyricsPluginInfoModifier>
) : PluginEventHandler() {

    val subscribedPlayers = mutableMapOf<String, MutableMap<Long, LyricsSubscriptionConfig>>()
    private val lineHandlers = ConcurrentHashMap<AudioTrack, LineHandler>()

    override fun onWebSocketOpen(context: ISocketContext, resumed: Boolean) {
        this.subscribedPlayers[context.sessionId] = mutableMapOf()
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
            val config: LyricsSubscriptionConfig = plugin.subscribedPlayers[socketContext.sessionId]?.get(guildId) ?: return

            val lyrics = plugin.lyricsManager.loadLyrics(track, config.skipTrackSource)

            // the track can stop or change while the lyrics are loading
            if (player.playingTrack !== track) {
                return
            }

            if (lyrics == null || lyrics.lines == null || lyrics.lines!!.isEmpty()) {
                socketContext.sendMessage(LyricsNotFoundEvent.serializer(), LyricsNotFoundEvent(guildId.toString()))
                return
            }

            socketContext.sendMessage(LyricsFoundEvent.serializer(), LyricsFoundEvent(guildId.toString(), lyrics.toLyrics(plugin.pluginInfoModifiers)))

            plugin.startLines(socketContext, guildId, track, lyrics.lines!!)
        }

        override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
            plugin.lineHandlers.remove(track)
        }

    }

    fun startLines(context: ISocketContext, guildId: Long, track: AudioTrack, lines: List<AudioLyrics.Line>) {
        val handler = LineHandler(context, pluginInfoModifiers, guildId, track, lines)
        lineHandlers[track] = handler
        handler.start()
    }

    fun onSeek(track: AudioTrack, position: Long) {
        lineHandlers[track]?.onSeek(position)
    }

    data class LyricsSubscriptionConfig(
        val skipTrackSource: Boolean
    )

}