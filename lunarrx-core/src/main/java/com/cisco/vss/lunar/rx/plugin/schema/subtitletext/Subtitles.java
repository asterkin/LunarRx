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

	private enum State {
		EOL_OR_EMPTY() {
			@Override
			State append(final StringBuilder text, final String line) {
				text.append(line);
				return nextState(line);
			}
			
		},
		NOT_EMPTY() {
			@Override
			State append(final StringBuilder text, final String line) {
				if(!line.matches("\\r?\\n"))
					text.append(' ');
				text.append(line);
				return nextState(line);
			}
			
		};
		
		private static State nextState(final String line) {
			return line.endsWith("\n") ? EOL_OR_EMPTY : NOT_EMPTY;
		}
		
		abstract State append(final StringBuilder text, final String line);
		
	};
	
	public String getText() {
		final StringBuilder text = new StringBuilder();
		State state = State.EOL_OR_EMPTY;
		for(Subtitles.SubtitleLine line : data.lines)
			state = state.append(text, line.text);
		return text.toString();
	}

}
