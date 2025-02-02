package com.github.topi314.lavalyrics.plugin

import com.github.topi314.lavalyrics.LyricsManager
import com.github.topi314.lavalyrics.api.LyricsManagerConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class LyricsConfiguration(
    val config: LavaLyricsConfig
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(LyricsConfiguration::class.java)
    }

    @Bean
    open fun audioLyricsManagerSupplier(lyricsManagerConfigurations: Collection<LyricsManagerConfiguration>): LyricsManager {
        log.info("Creating LyricsManager")
        var manager = LyricsManager()

        for (config in lyricsManagerConfigurations) {
            manager = config.configure(manager)
        }

        log.info("Sorting LyricsManagers")
        manager.sortLyricsManagers(config.sources)
        log.info("LyricsManager sorted")
        return manager
    }
}
