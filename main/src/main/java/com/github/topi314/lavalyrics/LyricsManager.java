package com.github.topi314.lavalyrics;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LyricsManager {

	private final List<AudioLyricsManager> lyricsManagers;

	public LyricsManager() {
		this.lyricsManagers = new ArrayList<>();
	}

	public void registerLyricsManager(AudioLyricsManager lyricsManager) {
		lyricsManagers.add(lyricsManager);
	}

	public void sortLyricsManagers(List<String> lyricsManagers) {
		this.lyricsManagers.sort(Comparator.comparing(audioLyricsManager -> lyricsManagers.contains(audioLyricsManager.getSourceName())));
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
		if (!skipTrackSource) {
			var trackLyricsManager = track.getSourceManager();
			if (trackLyricsManager instanceof AudioLyricsManager) {
				var lyrics = ((AudioLyricsManager) trackLyricsManager).loadLyrics(track);
				if (lyrics != null) {
					return lyrics;
				}
			}
		}
		for (var lyricsManager : this.lyricsManagers) {
			var lyrics = lyricsManager.loadLyrics(track);
			if (lyrics != null) {
				return lyrics;
			}
		}
		return null;
	}
}
