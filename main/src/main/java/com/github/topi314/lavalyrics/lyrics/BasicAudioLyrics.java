package com.github.topi314.lavalyrics.lyrics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class BasicAudioLyrics implements AudioLyrics {

	protected String sourceName;
	protected String provider;
	protected String text;
	protected List<AudioLyrics.Line> lines;

	public BasicAudioLyrics(String sourceName, String provider, String text, List<AudioLyrics.Line> lines) {
		this.sourceName = sourceName;
		this.provider = provider;
		this.text = text;
		this.lines = lines;
	}

	@NotNull
	public String getSourceName() {
		return this.sourceName;
	}

	@Nullable
	public String getProvider() {
		return this.provider;
	}

	@Nullable
	public String getText() {
		return this.text;
	}

	@Nullable
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

		@NotNull
		public Duration getTimestamp() {
			return this.timestamp;
		}

		@Nullable
		public Duration getDuration() {
			return this.duration;
		}

		@NotNull
		public String getLine() {
			return this.line;
		}
	}

}
