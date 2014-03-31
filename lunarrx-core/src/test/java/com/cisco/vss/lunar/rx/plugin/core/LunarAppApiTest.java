package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.lunar.rx.mq.LunarMQException.StreamingError.LMQ_OK;
import static com.cisco.vss.rx.java.Conversions.object2JsonString;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import rx.functions.Action1;
import com.cisco.vss.lunar.rx.ConcurrentHttpServerStub;
import com.cisco.vss.lunar.rx.mq.LunarMQServerStub;
import com.cisco.vss.rx.java.ObjectHolder;

public class LunarAppApiTest {
	private static final String  LUNAR_HOST   = "localhost";
	private static final String  DEVELOPER_ID = "6871c4b35301671668ebf26ae46b6441";

	@Test
	public void testGetArrayResponse() throws IOException, InterruptedException {
		final LunarSource.Response SOURCES_RESPONSE = new LunarSource().new Response();
		SOURCES_RESPONSE.data = new LunarSource[] {
				new LunarSource(1, "source1"),
				new LunarSource(2, "source2")
			};		
		final String SOURCES_HTTP_RESPONSE = object2JsonString(LunarSource.Response.class).call(SOURCES_RESPONSE);
		@SuppressWarnings("serial")
		final Map<String, String> HTTP_RESPONSES = new HashMap<String,String>(){{
			put("/sources", SOURCES_HTTP_RESPONSE);
		}};
		
		final ConcurrentHttpServerStub lunarServer = new ConcurrentHttpServerStub(HTTP_RESPONSES);
		final Lunar                    lunar       = new Lunar(LUNAR_HOST,lunarServer.startServer(),DEVELOPER_ID);
		final ObjectHolder<Throwable>  error       = new ObjectHolder<Throwable>();
		final List<LunarSource>        result      = new ArrayList<LunarSource>();
		
		lunar.getArrayResponse("/sources", LunarSource.Response.class, LunarSource.class).subscribe(
				new Action1<LunarSource>() {
					@Override
					public void call(final LunarSource source) {
						result.add(source);
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
		assertNull(error.value);
		assertArrayEquals(SOURCES_RESPONSE.data, result.toArray());
	}
	
	@Test
	public void testGetStatusUpdatesStream() throws IOException, InterruptedException {
		final LunarSource.StatusUpdateMessage up = new LunarSource().new StatusUpdateMessage();
		up.status = LunarSource.StatusUpdateMessage.Status.UP;
		up.list = new LunarSource[] {
			new LunarSource(3, "source3"),
			new LunarSource(4, "source4")
		};
		final String jsonUp = object2JsonString(LunarSource.StatusUpdateMessage.class).call(up);
		final LunarSource.StatusUpdateMessage down = new LunarSource().new StatusUpdateMessage();
		down.status = LunarSource.StatusUpdateMessage.Status.DOWN;
		down.list = new LunarSource[] {
			new LunarSource(3, "source3")
		};
		final String jsonDown = object2JsonString(LunarSource.StatusUpdateMessage.class).call(down);
		
		final byte[][] MQ_RESPONSES  = new byte[][]{
				LMQ_OK.GetMessage().getBytes(),
				jsonUp.getBytes(),
				jsonDown.getBytes()
		};
		final LunarMQServerStub mqServer    = new LunarMQServerStub(MQ_RESPONSES);
		final LunarUrlData.Response UPDATES_RESPONSE = new LunarUrlData().new Response();
		UPDATES_RESPONSE.data = new LunarUrlData();
		UPDATES_RESPONSE.data.url = String.format("localhost:%d/sourceUpdates", mqServer.startServer());
		final String UPDATES_HTTP_RESPONSE = object2JsonString(LunarUrlData.Response.class).call(UPDATES_RESPONSE);
		@SuppressWarnings("serial")
		final Map<String, String> HTTP_RESPONSES = new HashMap<String,String>(){{
			put("/updates/sources", UPDATES_HTTP_RESPONSE);
		}};
		
		final ConcurrentHttpServerStub  lunarServer = new ConcurrentHttpServerStub(HTTP_RESPONSES);
		final Lunar                     lunar       = new Lunar(LUNAR_HOST,lunarServer.startServer(),DEVELOPER_ID);
		final ObjectHolder<Throwable>   error       = new ObjectHolder<Throwable>();
		final Map<Integer, LunarSource> map         = new HashMap<Integer, LunarSource>();
		
		lunar.getStatusUpdatesStream("sources", LunarSource.StatusUpdateMessage.class, LunarSource.class).subscribe(
				new Action1<LunarNotify<LunarSource>>() {
					@Override
					public void call(final LunarNotify<LunarSource> notify) {
						final LunarSource source = notify.getItem();
						if(notify instanceof LunarAdd<?>) 
							map.put(source.sourceID, source);
						else if(notify instanceof LunarRemove<?>)
							map.remove(source.sourceID);
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
		assertEquals(1, map.size());
		assertArrayEquals(new LunarSource[]{new LunarSource(4,"source4")}, map.values().toArray());
	}
	
	@Test
	public void testGetNotifyStream() throws IOException, InterruptedException {
		final LunarSource.StatusUpdateMessage up = new LunarSource().new StatusUpdateMessage();
		up.status = LunarSource.StatusUpdateMessage.Status.UP;
		up.list = new LunarSource[] {
			new LunarSource(4, "source4"),
			new LunarSource(5, "source5")
		};
		final String jsonUp = object2JsonString(LunarSource.StatusUpdateMessage.class).call(up);
		final LunarSource.StatusUpdateMessage down = new LunarSource().new StatusUpdateMessage();
		down.status = LunarSource.StatusUpdateMessage.Status.DOWN;
		down.list = new LunarSource[] {
			new LunarSource(3, "source3")
		};
		final String jsonDown = object2JsonString(LunarSource.StatusUpdateMessage.class).call(down);
		
		final byte[][] MQ_RESPONSES  = new byte[][]{
				LMQ_OK.GetMessage().getBytes(),
				jsonDown.getBytes(),
				jsonUp.getBytes()
		};
		final LunarMQServerStub mqServer    = new LunarMQServerStub(MQ_RESPONSES);
		final LunarSource.Response SOURCES_RESPONSE = new LunarSource().new Response();
		SOURCES_RESPONSE.data = new LunarSource[] {
				new LunarSource(1, "source1"),
				new LunarSource(2, "source2"),
				new LunarSource(3, "source3")				
			};		
		final String SOURCES_HTTP_RESPONSE = object2JsonString(LunarSource.Response.class).call(SOURCES_RESPONSE);
		final LunarUrlData.Response UPDATES_RESPONSE = new LunarUrlData().new Response();
		UPDATES_RESPONSE.data = new LunarUrlData();
		UPDATES_RESPONSE.data.url = String.format("localhost:%d/sourceUpdates", mqServer.startServer());
		final String UPDATES_HTTP_RESPONSE = object2JsonString(LunarUrlData.Response.class).call(UPDATES_RESPONSE);
		@SuppressWarnings("serial")
		final Map<String, String> HTTP_RESPONSES = new HashMap<String,String>(){{
			put("/sources", SOURCES_HTTP_RESPONSE);
			put("/updates/sources", UPDATES_HTTP_RESPONSE);
		}};
		final ConcurrentHttpServerStub  lunarServer = new ConcurrentHttpServerStub(HTTP_RESPONSES);
		final Lunar                     lunar       = new Lunar(LUNAR_HOST,lunarServer.startServer(),DEVELOPER_ID);
		final ObjectHolder<Throwable>   error       = new ObjectHolder<Throwable>();
		final Map<Integer, LunarSource> map         = new HashMap<Integer, LunarSource>();
		final LunarSource[]             EXPECTED    = new LunarSource[] {
				new LunarSource(1, "source1"),
				new LunarSource(2, "source2"),
				new LunarSource(4, "source4"),
				new LunarSource(5, "source5")			
		};
		lunar.getCombinedNotifyStream("sources", LunarSource.StatusUpdateMessage.class, LunarSource.Response.class, LunarSource.class).subscribe(
				new Action1<LunarNotify<LunarSource>>() {
					@Override
					public void call(final LunarNotify<LunarSource> notify) {
						final LunarSource source = notify.getItem();
						if(notify instanceof LunarAdd<?>) 
							map.put(source.sourceID, source);
						else if(notify instanceof LunarRemove<?>)
							map.remove(source.sourceID);
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
		assertEquals(EXPECTED.length, map.size());
		assertArrayEquals(EXPECTED, map.values().toArray());
	}
	
	@Test
	public void testGetBitestream() throws IOException, InterruptedException {
		final String PLUGIN_NAME     = "source_stream";
		final String TRACK_NAME      = "stream";
		final int    SOURCE_ID       = 1;
		final byte[][] MQ_RESPONSES  = new byte[][]{
				LMQ_OK.GetMessage().getBytes(),
				"ABCDEFG".getBytes()
		};
		final LunarMQServerStub mqServer = new LunarMQServerStub(MQ_RESPONSES);
		final LunarTrack        track    = new LunarTrack(SOURCE_ID,PLUGIN_NAME,TRACK_NAME);
		track.url = String.format("localhost:%d/stream:2.2041.9211", mqServer.startServer());
		final ObjectHolder<Throwable> error = new ObjectHolder<Throwable>();
		
		track.getBitestream()
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
	
	class SampleTrackItem extends TrackItem {

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
				LMQ_OK.GetMessage().getBytes(),
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
}
