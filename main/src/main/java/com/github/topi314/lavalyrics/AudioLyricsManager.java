package com.github.topi314.lavalyrics;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AudioLyricsManager {

	@NotNull
	String getSourceName();

	@Nullable
	AudioLyrics loadLyrics(@NotNull AudioTrack track);

	void shutdown();
}
