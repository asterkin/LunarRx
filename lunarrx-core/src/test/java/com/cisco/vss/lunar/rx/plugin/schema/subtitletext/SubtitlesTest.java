package com.cisco.vss.lunar.rx.plugin.schema.subtitletext;

import static org.junit.Assert.*;
import org.junit.Test;

public class SubtitlesTest {

	@Test
	public void testGetTextUnix() {
		  final Subtitles subtitles = makeSubtitles("\n");
		  
		  assertEquals("Xxxx yyyy xxxx Yyyy\n", subtitles.getText());
	}

	@Test
	public void testGetTextWindows() {
		  final Subtitles subtitles = makeSubtitles("\r\n");
		  
		  assertEquals("Xxxx yyyy xxxx Yyyy\r\n", subtitles.getText());
	}
	
	private Subtitles makeSubtitles(final String lastLine) {
		final Subtitles              subtitles = new Subtitles();
		final Subtitles.SubtitleLine line1     = subtitles.new SubtitleLine("Xxxx yyyy");
		final Subtitles.SubtitleLine line2     = subtitles.new SubtitleLine("xxxx Yyyy");
		final Subtitles.SubtitleLine line3     = subtitles.new SubtitleLine(lastLine);
		final Subtitles.Data         data      = subtitles.new Data();
		   
		data.lines    = new Subtitles.SubtitleLine[] {line1, line2, line3};
		subtitles.data= data;
		
		return subtitles;
	}
	
}
