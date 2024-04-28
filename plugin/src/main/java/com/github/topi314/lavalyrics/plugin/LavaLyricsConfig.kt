package com.github.topi314.lavalyrics.plugin

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "plugins.lavalyrics")
@Component
data class LavaLyricsConfig(
    var sources: List<String> = listOf()
)