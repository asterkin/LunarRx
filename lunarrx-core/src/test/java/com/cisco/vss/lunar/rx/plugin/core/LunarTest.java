package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.rx.java.Conversions.object2JsonString;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import rx.functions.Action1;

import com.cisco.vss.lunar.rx.mq.LunarMQServerStub;
import com.cisco.vss.rx.java.ObjectHolder;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class LunarTest {
	private static final String  LUNAR_HOST   = "localhost";
	private static final int     LUNAR_PORT   = 3000;
	private static final String  DEVELOPER_ID = "6871c4b35301671668ebf26ae46b6441";

	@ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(LUNAR_PORT);
    @Rule
    public WireMockClassRule instanceRule = wireMockRule;

	private Lunar                          lunar;
	private final ObjectHolder<Throwable>  error = new ObjectHolder<Throwable>();
    
	@Before
	public void setUp() {
		lunar       = new Lunar(LUNAR_HOST, LUNAR_PORT, DEVELOPER_ID);
		error.value = null;
	}
	
	@Test
	public void testGetArrayResponse() throws IOException, InterruptedException {
		final LunarSource.Response SOURCES_RESPONSE = new LunarSource().new Response();
		SOURCES_RESPONSE.data = new LunarSource[] {
				new LunarSource(1, "source1"),
				new LunarSource(2, "source2")
			};		
		final String SOURCES_HTTP_RESPONSE = object2JsonString(LunarSource.Response.class).call(SOURCES_RESPONSE);
    	stubFor(get(urlEqualTo("/sources"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(SOURCES_HTTP_RESPONSE)));
		
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
				"OK".getBytes(),
				jsonUp.getBytes(),
				jsonDown.getBytes()
		};
		final LunarMQServerStub     mqServer         = new LunarMQServerStub(MQ_RESPONSES);
		final LunarUrlData.Response UPDATES_RESPONSE = new LunarUrlData().new Response();
		UPDATES_RESPONSE.data = new LunarUrlData();
		UPDATES_RESPONSE.data.url = String.format("localhost:%d/sourceUpdates", mqServer.startServer());
		final String UPDATES_HTTP_RESPONSE = object2JsonString(LunarUrlData.Response.class).call(UPDATES_RESPONSE);
		
		final Map<Integer, LunarSource> map= new HashMap<Integer, LunarSource>();
		
    	stubFor(get(urlEqualTo("/updates/sources"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(UPDATES_HTTP_RESPONSE)));
    	
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
		assertNull(error.value);
		assertEquals(1, map.size());
		assertArrayEquals(new LunarSource[]{new LunarSource(4,"source4")}, map.values().toArray());
	}
	
	@Test
	public void testReporting() throws IOException, InterruptedException {
		final LunarResponse            RESPONSE      = new LunarResponse();
		final String                   HTTP_RESPONSE = object2JsonString(LunarResponse.class).call(RESPONSE);
		final LunarTrack               TRACK         = new LunarTrack(1, "plugin1", "track1"); 
		final LunarPluginStateReport   REPORT        = LunarPluginStateReport.running(DEVELOPER_ID, TRACK);
		final String                   REPORT_JSON   = object2JsonString(LunarPluginStateReport.class).call(REPORT); 

    	stubFor(post(urlEqualTo("/state/plugins"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(HTTP_RESPONSE)));
		
		lunar.running(TRACK);
		
        verify(postRequestedFor(urlMatching("/state/plugins"))
                .withRequestBody(equalTo(REPORT_JSON))
                .withHeader("Content-Type", equalTo("application/json")));		
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
		
		lunar.getInputTrackStream(track)
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
		
		lunar.getInputTrackStream(track, SampleTrackItem.class).subscribe(
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
