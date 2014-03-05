package com.cisco.vss.lunar.rx.plugin.schema;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import rx.functions.Action1;
import static com.cisco.vss.rx.java.Conversions.*;

public class SubtitlesTrackItemTest {

	@Test
	public void testFromJson() throws IOException {
		final String path    = Thread.currentThread().getContextClassLoader().getResource("SubtitlesTestData.json").getPath();
		final byte[] encoded = Files.readAllBytes(Paths.get(path));
		final String json    = new String(encoded);
		final String EXPECTED= "dictate you not arrive\nempty--handed.\n";
		
		jsonString2Object(SubtitlesTrackItem.class).call(json).subscribe(
			new Action1<SubtitlesTrackItem>() {
				@Override
				public void call(final SubtitlesTrackItem item) {
					assertEquals("subtitles", item.getTrackName());
					assertEquals(new Long(290202171), item.getPts());
					assertEquals(EXPECTED, item.getText());
				}				
			}
		);
		
	}

}
