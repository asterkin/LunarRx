package com.cisco.vss.lunar.rx.plugin.schema.subtitletext;

import com.cisco.vss.lunar.rx.plugin.core.LunarTrackItem;

public class Subtitles extends LunarTrackItem {

	public class SubtitleLine {
		public SubtitleLine(final String text) {
			this.text = text;
		}
		
		public String text;
	}
	
	public class Data {
		public Long           pts;
		public SubtitleLine[] lines;
	} 
	
	public Data data;

	public Long getPts() {
		return data.pts;
	}

	public String getText() {
		final StringBuilder text =new StringBuilder();
		for(Subtitles.SubtitleLine l : data.lines)
			text.append(l.text);
		return text.toString();
	}

}
