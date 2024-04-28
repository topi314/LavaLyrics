package com.github.topi314.lavalyrics.plugin

import com.github.topi314.lavalyrics.LyricsManager
import com.github.topi314.lavalyrics.api.LyricsManagerConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class LyricsConfiguration(
    val config: LavaLyricsConfig
) {
    @Bean
    open fun audioLyricsManagerSupplier(lyricsManagerConfigurations: Collection<LyricsManagerConfiguration>): LyricsManager {
        var manager = LyricsManager()

        for (config in lyricsManagerConfigurations) {
            manager = config.configure(manager)
        }

        manager.sortLyricsManagers(config.sources)
        return manager
    }
}
