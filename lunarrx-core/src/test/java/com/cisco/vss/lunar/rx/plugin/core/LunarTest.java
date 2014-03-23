package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.lunar.rx.mq.LunarMQException.StreamingError.LMQ_OK;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import com.cisco.vss.lunar.rx.mq.LunarMQServerStub;
import com.cisco.vss.lunar.rx.plugin.core.Lunar;
import com.cisco.vss.lunar.rx.plugin.core.LunarTrack;
import com.cisco.vss.lunar.rx.plugin.core.TrackItem;
import com.cisco.vss.rx.java.HttpServerStub;
import com.cisco.vss.rx.java.ObjectHolder;

import static com.cisco.vss.rx.java.Conversions.*;
import rx.functions.Action1;

public class LunarTest {
	private static final String  LUNAR_HOST   = "localhost";
	private static final String  DEVELOPER_ID = "6871c4b35301671668ebf26ae46b6441";
	private static final Integer SOURCE_ID    = 1;
	
	@Ignore //TODO
	@Test
	public void testInputTrackStream() throws IOException, InterruptedException {
		final String PLUGIN_NAME     = "source_stream";
		final String TRACK_NAME      = "stream";
		final byte[][] MQ_RESPONSES  = new byte[][]{
				LMQ_OK.GetMessage().getBytes(),
				"ABCDEFG".getBytes()
		};
		final LunarMQServerStub mqServer    = new LunarMQServerStub(MQ_RESPONSES);
		final LunarTrack         trackInfo   = new LunarTrack(SOURCE_ID,PLUGIN_NAME,TRACK_NAME);
		trackInfo.url = String.format("localhost:%d/stream:2.2041.9211", mqServer.startServer());
		final TrackInfoResponse response = new TrackInfoResponse();
		response.result = LunarResponseResult.OK;
		response.data   = new LunarTrack[]{trackInfo};
		final byte[][]          HTTP_RESPONSES = new byte[][]{
			object2JsonString(TrackInfoResponse.class).call(response).getBytes()
		};
		final HttpServerStub lunarServer = new HttpServerStub(HTTP_RESPONSES);
		final Lunar          lunar       = new Lunar(LUNAR_HOST,lunarServer.startServer(),DEVELOPER_ID);
		final ObjectHolder<Throwable> error = new ObjectHolder<Throwable>();
		
		lunar.getInputTrackStream(SOURCE_ID, PLUGIN_NAME, TRACK_NAME)
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
		lunarServer.join();
		mqServer.join();
		assertNull(error.value);
	}

	class SampleTrackItem extends TrackItem {

		public SampleTrackItem(int sourceID, Date time, String pluginName,
				String trackName, int trackVersion) {
			super(sourceID, time, pluginName, trackName, trackVersion);
		}
		
		public String[] lines;
		public Long     pts;
	}
	
	@Ignore //TODO
	@Test
	public void testInputTrackItemStream() throws IOException, InterruptedException {
		final String PLUGIN_NAME   = "subtitletext";
		final String TRACK_NAME    = "subtitles";
		final SampleTrackItem item = new SampleTrackItem(1, new Date(), PLUGIN_NAME, TRACK_NAME, 1);
		item.pts = 297451166L;
		item.lines = new String[] {
			"First line",
			"",
			"Second line"
		};
		final String itemJson = object2JsonString(SampleTrackItem.class).call(item);
		final byte[][] MQ_RESPONSES  = new byte[][]{
				LMQ_OK.GetMessage().getBytes(),
				itemJson.getBytes()
		};
		final LunarMQServerStub mqServer    = new LunarMQServerStub(MQ_RESPONSES);
		final LunarTrack         trackInfo   = new LunarTrack(SOURCE_ID,PLUGIN_NAME,TRACK_NAME);
		trackInfo.url = String.format("localhost:%d/subtitletext:subtitles:1", mqServer.startServer());
		final TrackInfoResponse response = new TrackInfoResponse();
		response.result = LunarResponseResult.OK;
		response.data   = new LunarTrack[]{trackInfo};
		final byte[][] HTTP_RESPONSES = new byte[][]{
			object2JsonString(TrackInfoResponse.class).call(response).getBytes()
		};
		final HttpServerStub lunarServer    = new HttpServerStub(HTTP_RESPONSES);
		final Lunar          lunar          = new Lunar(LUNAR_HOST,lunarServer.startServer(),DEVELOPER_ID);
		final ObjectHolder<Throwable> error = new ObjectHolder<Throwable>();

		lunar.getInputTrackItemStream(SampleTrackItem.class, SOURCE_ID, PLUGIN_NAME, TRACK_NAME)
		.subscribe(
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
		lunarServer.join();
		assertNull(error.value);
	}
}
