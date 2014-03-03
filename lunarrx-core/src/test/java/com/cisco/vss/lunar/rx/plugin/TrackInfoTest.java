package com.cisco.vss.lunar.rx.plugin;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cisco.vss.lunar.rx.plugin.core.TrackInfo;
import com.google.gson.Gson;

public class TrackInfoTest {

	@Test
	public void testFromJson() {
		final String input =
 	   "{"
			+"\"sourceID\":\"1234\","
 			+"\"protocol\":\"LunarMQ\","
			+"\"track\":\"track1\","
			+"\"deployed\":true,"
			+"\"mime\":\"json\","
			+"\"url\":\"localhost:8080/tracks/track1\","
 			+"\"plugin\":\"plugin1\""
		+"}"
				;
		final Gson      gson      = new Gson();
		final TrackInfo trackInfo = gson.fromJson(input, TrackInfo.class);
		assertEquals("1234",                         trackInfo.sourceID);
		assertEquals("LunarMQ",                      trackInfo.protocol);
		assertEquals("track1",                       trackInfo.trackName);
		assertEquals("json",                         trackInfo.mime);
		assertEquals("localhost:8080/tracks/track1", trackInfo.url);
		assertEquals("plugin1",                      trackInfo.pluginName);
		assertTrue(trackInfo.deployed);
	}

	@Test
	public void testHttpRequestArgs() {
		final TrackInfo trackInfo = new TrackInfo("1234", "plugin1", "track1");
		final String    path      = trackInfo.httpGetRequestPath();
		final String    EXPECTED  = "/tracks?sourceID=1234&pluginName=plugin1&trackName=track1";

		assertEquals(EXPECTED, path);
	}
}
