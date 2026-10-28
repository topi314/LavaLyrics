package com.github.topi314.lavalyrics;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LyricsManager {

	private static final Logger log = LoggerFactory.getLogger(LyricsManager.class);
	private final List<AudioLyricsManager> lyricsManagers;

	public LyricsManager() {
		this.lyricsManagers = new ArrayList<>();
	}

	public void registerLyricsManager(AudioLyricsManager lyricsManager) {
		lyricsManagers.add(lyricsManager);
	}

	public void sortLyricsManagers(List<String> lyricsManagers) {
		log.info("Sorting lyrics managers");
		// sources in the configured order, sources missing from the config after them
		this.lyricsManagers.sort(Comparator.comparingInt(audioLyricsManager -> {
			var index = lyricsManagers.indexOf(audioLyricsManager.getSourceName());
			return index == -1 ? Integer.MAX_VALUE : index;
		}));
		log.info("Sorted lyrics managers");
	}

	@Nullable
	public <T extends AudioLyricsManager> T lyrics(Class<T> klass) {
		for (var lyricsManager : lyricsManagers) {
			if (klass.isAssignableFrom(lyricsManager.getClass())) {
				return klass.cast(lyricsManager);
			}
		}

		return null;
	}

	public List<AudioLyricsManager> getLyricsManagers() {
		return Collections.unmodifiableList(this.lyricsManagers);
	}

	public void shutdown() {
		for (var lyricsManager : this.lyricsManagers) {
			lyricsManager.shutdown();
		}
	}

	@Nullable
	public AudioLyrics loadLyrics(AudioTrack track) {
		return loadLyrics(track, false);
	}

	@Nullable
	public AudioLyrics loadLyrics(AudioTrack track, boolean skipTrackSource) {
		if (this.lyricsManagers.isEmpty()) {
			throw new IllegalStateException("No lyrics managers registered");
		}
		var exceptions = new ArrayList<RuntimeException>();
		var attempts = 0;
		AudioLyricsManager triedLyricsManager = null;
		if (!skipTrackSource) {
			var trackLyricsManagerName = track.getSourceManager().getSourceName();
			var trackLyricsManager = this.lyricsManagers.stream().filter(audioLyricsManager -> audioLyricsManager.getSourceName().equals(trackLyricsManagerName)).findFirst();
			if (trackLyricsManager.isPresent()) {
				triedLyricsManager = trackLyricsManager.get();
				attempts++;
				var lyrics = loadLyrics(triedLyricsManager, track, exceptions);
				if (lyrics != null) {
					return lyrics;
				}
			}
		}
		for (var lyricsManager : this.lyricsManagers) {
			if (lyricsManager == triedLyricsManager) {
				continue;
			}
			attempts++;
			var lyrics = loadLyrics(lyricsManager, track, exceptions);
			if (lyrics != null) {
				return lyrics;
			}
		}
		// only fail when every source failed, if one of them answered there are just no lyrics
		if (!exceptions.isEmpty() && exceptions.size() == attempts) {
			var exception = exceptions.get(0);
			for (var i = 1; i < exceptions.size(); i++) {
				exception.addSuppressed(exceptions.get(i));
			}
			throw exception;
		}
		return null;
	}

	@Nullable
	private AudioLyrics loadLyrics(AudioLyricsManager lyricsManager, AudioTrack track, List<RuntimeException> exceptions) {
		try {
			return lyricsManager.loadLyrics(track);
		} catch (RuntimeException e) {
			log.warn("Failed to load lyrics from source {}: {}", lyricsManager.getSourceName(), e.getMessage());
			exceptions.add(e);
			return null;
		}
	}
}
