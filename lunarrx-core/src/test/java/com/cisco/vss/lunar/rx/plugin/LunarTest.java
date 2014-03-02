package com.cisco.vss.lunar.rx.plugin;

import static com.cisco.vss.lunar.rx.mq.LunarMQException.StreamingError.LMQ_OK;
import static org.junit.Assert.*;
import java.io.IOException;
import org.junit.Test;
import com.cisco.vss.lunar.rx.mq.LunarMQServerStub;
import com.cisco.vss.rx.java.HttpServerStub;
import com.google.gson.Gson;
import rx.functions.Action1;

public class LunarTest {
	private static final String LUNAR_HOST   = "localhost";
	private static final String DEVELOPER_ID = "6871c4b35301671668ebf26ae46b6441";
	private Lunar        lunar;
	
	@Test
	public void testInputTrackStream() throws IOException {
		final String SOURCE_ID              = "1";
		final String PLUGIN_NAME            = "source_stream";
		final String TRACK_NAME             = "stream";
		final byte[][] MQ_RESPONSES         = new byte[][]{
				LMQ_OK.GetMessage().getBytes(),
				"ABCEFG".getBytes()
		};
		final LunarMQServerStub mqServer    = new LunarMQServerStub(MQ_RESPONSES);
		final TrackInfo         trackInfo   = new TrackInfo(SOURCE_ID,PLUGIN_NAME,TRACK_NAME);
		final Gson              gson        = new Gson();
		trackInfo.url = String.format("localhost:%d/stream:2.2041.9211", mqServer.startServer());
		final byte[][]          HTTP_RESPONSES = new byte[][]{
			gson.toJson(trackInfo).getBytes()
		};
		final HttpServerStub    lunarServer = new HttpServerStub(HTTP_RESPONSES);
		lunar = new Lunar(LUNAR_HOST,lunarServer.startServer(),DEVELOPER_ID);
		
		lunar.getInputTrackStream(SOURCE_ID, PLUGIN_NAME, TRACK_NAME)
		.subscribe(
			new Action1<byte[]>() {
				@Override
				public void call(byte[] message) {
					assertArrayEquals("ABCDEFG".getBytes(), message);
				}
			}
		);
	}
}
