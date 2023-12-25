package com.github.topi314.lavalyrics.api

import com.github.topi314.lavalyrics.LyricsManager

interface LyricsManagerConfiguration {
    fun configure(manager: LyricsManager): LyricsManager
}
