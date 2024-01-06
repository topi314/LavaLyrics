package com.github.topi314.lavalyrics.lyrics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public interface AudioLyrics {

	@NotNull
	String getSourceName();

	@Nullable
	String getOriginalSourceName();

	@Nullable
	String getText();

	@Nullable
	List<Line> getLines();

	interface Line {
		@NotNull
		Duration getTimestamp();

		@Nullable
		Duration getDuration();

		@NotNull
		String getLine();
	}

}
