package com.cisco.vss.lunar.rx.plugin.schema.subtitletext;

import com.cisco.vss.lunar.rx.plugin.core.TrackItem;

public class Subtitles extends TrackItem {

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
		String text = "";
		for(Subtitles.SubtitleLine l : data.lines)
			text += l.text;
		return text;
	}

}
