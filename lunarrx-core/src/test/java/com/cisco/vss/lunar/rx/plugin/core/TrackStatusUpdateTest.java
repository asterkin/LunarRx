package com.cisco.vss.lunar.rx.plugin.core;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cisco.vss.lunar.rx.plugin.core.TrackStatus;
import com.cisco.vss.lunar.rx.plugin.core.TracksStatusUpdate;
import com.google.gson.Gson;

public class TrackStatusUpdateTest {

	@Test
	public void testJsonConversion() {
		final String input =
				"{\"messageType\":\"tracks\","
						 +"\"status\":\"up\","
						 +"\"list\":["
						   +"{\"pluginName\":\"plugin1\","
						     +"\"trackName\":\"track1\","
						     +"\"sourceID\":1234,"
						     +"\"mime\":\"json\","
						     +"\"url\":\"localhost:8080/tracks/track1\","
						     +"\"protocols\" : ["
						        +"{\"protocol\":\"http\","
						        +" \"url\":\"localhost:9090/track1\""
						        +"}"
						     +"]"
						   +"}"
						+"]"
					+"}"
				;
		final Gson gson = new Gson();
		TracksStatusUpdate record = gson.fromJson(input, TracksStatusUpdate.class);
		assertEquals(TrackStatus.TRACK_IS_UP, record.status);
		assertEquals(1, record.list.length);
//		assertEquals("plugin1", record.list[0].pluginName);
//		assertEquals("track1", record.list[0].trackName);
	}
}
