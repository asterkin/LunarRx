package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.rx.java.Conversions.object2JsonString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.*;
import java.io.IOException;
import java.util.Date;
import org.junit.Test;
import rx.functions.Action1;
import com.cisco.vss.lunar.rx.mq.LunarMQServerStub;
import com.cisco.vss.rx.java.ObjectHolder;
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
	public void testGetBytestream() throws IOException, InterruptedException {
		final String PLUGIN_NAME     = "source_stream";
		final String TRACK_NAME      = "stream";
		final int    SOURCE_ID       = 1;
		final byte[][] MQ_RESPONSES  = new byte[][]{
				"OK".getBytes(),
				"ABCDEFG".getBytes()
		};
		final LunarMQServerStub mqServer = new LunarMQServerStub(MQ_RESPONSES);
		final LunarTrack        track    = new LunarTrack(SOURCE_ID,PLUGIN_NAME,TRACK_NAME);
		track.url = String.format("localhost:%d/stream:2.2041.9211", mqServer.startServer());
		final ObjectHolder<Throwable> error = new ObjectHolder<Throwable>();
		
		track.getBytestream()
		.subscribe(
			new Action1<byte[]>() {
				@Override
				public void call(byte[] message) {
					assertArrayEquals("ABCDEFG".getBytes(), message);
				}
			},
			new Action1<Throwable>() {
				@Override
				public void call(Throwable err) {
					error.value = err;
				}
				
			}
		);
		mqServer.join();
		assertNull(error.value);	
	}
	
	class SampleTrackItem extends LunarTrackItem {

		public SampleTrackItem(int sourceID, Date time, String pluginName,
				String trackName, int trackVersion) {
			super(sourceID, time, pluginName, trackName, trackVersion);
		}
		
		public String[] lines;
		public Long     pts;
	}
		
	@Test
	public void testGetItems() throws IOException, InterruptedException {
		final String PLUGIN_NAME   = "subtitletext";
		final String TRACK_NAME    = "subtitles";
		final int    SOURCE_ID     = 1;
		final SampleTrackItem item = new SampleTrackItem(1, new Date(), PLUGIN_NAME, TRACK_NAME, 1);
		item.pts = 297451166L;
		item.lines = new String[] {
			"First line",
			"",
			"Second line"
		};
		final String itemJson = object2JsonString(SampleTrackItem.class).call(item);
		final byte[][] MQ_RESPONSES  = new byte[][]{
				"OK".getBytes(),
				itemJson.getBytes()
		};
		final LunarMQServerStub mqServer  = new LunarMQServerStub(MQ_RESPONSES);
		final LunarTrack        track     = new LunarTrack(SOURCE_ID,PLUGIN_NAME,TRACK_NAME);
		track.url = String.format("localhost:%d/subtitletext:subtitles:1", mqServer.startServer());
		final ObjectHolder<Throwable> error = new ObjectHolder<Throwable>();
		
		track.getItems(SampleTrackItem.class).subscribe(
				new Action1<SampleTrackItem>() {
					@Override
					public void call(final SampleTrackItem it) {
						assertEquals(item.pts, it.pts);
						assertArrayEquals(item.lines, it.lines);
					}
				},
				new Action1<Throwable>() {
					@Override
					public void call(Throwable err) {
						error.value = err;
					}

				}
				);
		mqServer.join();
		assertNull(error.value);		
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
