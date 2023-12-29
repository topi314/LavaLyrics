package com.github.topi314.lavalyrics.lyrics;

import java.time.Duration;
import java.util.List;

public interface AudioLyrics {

	String getSourceName();

	String getText();

	List<Line> getLines();

	interface Line {
		Duration getTimestamp();

		Duration getDuration();

		String getLine();
	}

}
