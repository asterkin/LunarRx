package com.cisco.vss.lunar.rx.plugin.schema;

import java.util.Date;

import com.cisco.vss.lunar.rx.plugin.core.TrackItem;

public class SubtitlesTrackItem extends TrackItem {

	public SubtitlesTrackItem(int sourceID, Date time, String pluginName, String trackName, int trackVersion) {
		super(sourceID, time, pluginName, trackName, trackVersion);
		data = new Data();
	}
	
	class SubtitleLine {
		public SubtitleLine(final String text) {
			this.text = text;
		}
		
		public String text;
	}
	
	class Data {
		public Long           pts;
		public SubtitleLine[] lines;
	} 
	
	public Data data;

	public Long getPts() {
		return data.pts;
	}

	public String getText() {
		String text = "";
		for(SubtitlesTrackItem.SubtitleLine l : data.lines)
			text += l.text;
		return text;
	}

}
