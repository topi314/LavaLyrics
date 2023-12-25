package com.github.topi314.lavalyrics.result;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class BasicAudioLyrics implements AudioLyrics {

	protected String text;
	protected List<AudioLyrics.Line> lines;

	public BasicAudioLyrics(String text, List<AudioLyrics.Line> lines) {
		this.text = text;
		this.lines = lines;
	}

	public String getText() {
		return this.text;
	}

	public List<AudioLyrics.Line> getLines() {
		return this.lines;
	}

	public static class BasicLine implements AudioLyrics.Line {

		protected Duration timestamp;
		protected Duration duration;
		protected String line;

		public BasicLine(Duration timestamp, Duration duration, String line) {
			this.timestamp = timestamp;
			this.duration = duration;
			this.line = line;
		}

		public Duration getTimestamp() {
			return this.timestamp;
		}

		public Duration getDuration() {
			return this.duration;
		}

		public String getLine() {
			return this.line;
		}
	}

}
