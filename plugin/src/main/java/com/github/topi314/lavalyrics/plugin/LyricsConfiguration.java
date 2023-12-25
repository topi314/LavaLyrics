package com.github.topi314.lavalyrics.plugin;

import com.github.topi314.lavalyrics.LyricsManager;
import com.github.topi314.lavalyrics.api.LyricsManagerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
public class LyricsConfiguration {

	@Bean
	public LyricsManager audioLyricsManagerSupplier(Collection<LyricsManagerConfiguration> lyricsManagerConfigurations) {
		var manager = new LyricsManager();

		for (var config : lyricsManagerConfigurations) {
			manager = config.configure(manager);
		}

		return manager;
	}

}
