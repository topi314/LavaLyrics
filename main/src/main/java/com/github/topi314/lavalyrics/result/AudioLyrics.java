package com.github.topi314.lavalyrics.result;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public interface AudioLyrics {

	String getText();

	List<Line> getLines();

	interface Line {
		Duration getTimestamp();

		Duration getDuration();

		String getLine();
	}

}
