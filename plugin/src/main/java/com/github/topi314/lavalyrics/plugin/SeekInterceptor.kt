package com.github.topi314.lavalyrics.plugin

import dev.arbjerg.lavalink.api.ISocketServer
import dev.arbjerg.lavalink.api.RestInterceptor
import dev.arbjerg.lavalink.protocol.v4.Omissible
import dev.arbjerg.lavalink.protocol.v4.PlayerUpdate
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.MethodParameter
import org.springframework.http.HttpInputMessage
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter
import java.lang.reflect.Type

private const val SEEK_POSITION_ATTRIBUTE = "lavalyrics.seekPosition"

// lavaplayer has no seek event, so seeks are picked up from the update player requests
@Component
class SeekInterceptor(
    private val socketServer: ISocketServer,
    private val lavaLyricsPluginEventHandler: LavaLyricsPluginEventHandler
) : RestInterceptor {

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
        val position = request.getAttribute(SEEK_POSITION_ATTRIBUTE) as? Long ?: return
        if (ex != null || response.status >= 400) {
            return
        }

        // /v4/sessions/{sessionId}/players/{guildId}
        val parts = request.requestURI.split('/')
        if (parts.size != 6 || parts[1] != "v4" || parts[2] != "sessions" || parts[4] != "players") {
            return
        }

        val guildId = parts[5].toLongOrNull() ?: return
        val track = socketServer.sessions[parts[3]]?.players?.get(guildId)?.track ?: return
        lavaLyricsPluginEventHandler.onSeek(track, position)
    }
}

// The seek position is taken from the request body, because right after a seek the track
// still reports the old position until the first frame from the new position is played.
@ControllerAdvice
class SeekPositionReader : RequestBodyAdviceAdapter() {

    override fun supports(methodParameter: MethodParameter, targetType: Type, converterType: Class<out HttpMessageConverter<*>>) =
        targetType == PlayerUpdate::class.java

    override fun afterBodyRead(body: Any, inputMessage: HttpInputMessage, parameter: MethodParameter, targetType: Type, converterType: Class<out HttpMessageConverter<*>>): Any {
        val update = body as PlayerUpdate
        val position = update.position

        // a position sent together with a new track is its start position, not a seek
        if (position is Omissible.Present && update.encodedTrack !is Omissible.Present && update.identifier !is Omissible.Present && update.track !is Omissible.Present) {
            RequestContextHolder.currentRequestAttributes().setAttribute(SEEK_POSITION_ATTRIBUTE, position.value, RequestAttributes.SCOPE_REQUEST)
        }
        return body
    }
}
