package com.cisco.vss.lunar.rx.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.google.gson.Gson;

public class LunarTrackTest {

	@Test
	public void testFromJson() {
		final String input =
 	   "{"
			+"\"sourceID\":1234,"
 			+"\"protocol\":\"LunarMQ\","
			+"\"track\":\"track1\","
			+"\"deployed\":true,"
			+"\"mime\":\"json\","
			+"\"url\":\"localhost:8080/tracks/track1\","
 			+"\"plugin\":\"plugin1\""
		+"}"
				;
		final Gson      gson      = new Gson();
		final LunarTrack trackInfo = gson.fromJson(input, LunarTrack.class);
		assertEquals(new Integer(1234),              trackInfo.sourceID);
		assertEquals("LunarMQ",                      trackInfo.protocol);
		assertEquals("track1",                       trackInfo.trackName);
		assertEquals("json",                         trackInfo.mime);
		assertEquals("localhost:8080/tracks/track1", trackInfo.url);
		assertEquals("plugin1",                      trackInfo.pluginName);
		assertTrue(trackInfo.deployed);
	}

	@Test
	public void testHttpRequestArgs() {
		final LunarTrack trackInfo = new LunarTrack(1234, "plugin1", "track1");
		final String    path      = trackInfo.httpGetRequestPath();
		final String    EXPECTED  = "/tracks?sourceID=1234&pluginName=plugin1&trackName=track1";

		assertEquals(EXPECTED, path);
	}
	
	@Test
	public void testStreamerPath() {
		final LunarTrack trackInfo    = new LunarTrack(1234, "plugin1", "track1");
	    final String    DEVELOPER_ID = "6871c4b35301671668ebf26ae46b6441";

		final String    path      = trackInfo.streamerRequestPath(DEVELOPER_ID);
		final String    EXPECTED  = "/streamer?sourceID=1234&pluginName=plugin1&trackName=track1&mimeType=json&enablePostToCore=false&protocol=LunarMQ&developerID="+DEVELOPER_ID;

		assertEquals(EXPECTED, path);
		
	}
	
	@Test
	public void testAttachToSource() {
		final LunarTrack template = new LunarTrack(null, "pluginA", "trackB");
		final LunarTrack result   = template.attachToSource(1);
		
		assertEquals(new Integer(1), result.sourceID);
		assertEquals("pluginA", result.pluginName);
		assertEquals("trackB",  result.trackName);
		assertFalse (template == result);
	}
}
