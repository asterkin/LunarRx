package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.lunar.rx.mq.LunarMQException.StreamingError.LMQ_OK;
import static com.cisco.vss.rx.java.Conversions.object2JsonString;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import rx.functions.Action1;

import com.cisco.vss.lunar.rx.mq.LunarMQServerStub;
import com.cisco.vss.rx.java.HttpServerStub;
import com.cisco.vss.rx.java.ObjectHolder;

public class LunarAppApiTest {
	private static final String  LUNAR_HOST   = "localhost";
	private static final String  DEVELOPER_ID = "6871c4b35301671668ebf26ae46b6441";

	@Ignore
	@Test
	public void testGetSourcesNotify() throws IOException, InterruptedException {
		final LunarSource.Response RESPONSE = new LunarSource().new Response();
		RESPONSE.data = new LunarSource[] {
				new LunarSource(1, "source1"),
				new LunarSource(2, "source2")
			};		
		final byte[][] HTTP_RESPONSES = new byte[][]{
			object2JsonString(LunarSource.Response.class).call(RESPONSE).getBytes()
  	    };
		final HttpServerStub                  lunarServer = new HttpServerStub(HTTP_RESPONSES);
		final Lunar                           lunar       = new Lunar(LUNAR_HOST,lunarServer.startServer(),DEVELOPER_ID);
		final ObjectHolder<Throwable>         error       = new ObjectHolder<Throwable>();
		//TODO: use list directly
		final ObjectHolder<List<LunarSource>> result      = new ObjectHolder<List<LunarSource>>(new ArrayList<LunarSource>());
		
		lunar.getSourcesNotify().subscribe(
				new Action1<LunarNotify<LunarSource>>() {
					@Override
					public void call(final LunarNotify<LunarSource> notify) {
						if((notify instanceof LunarAdd<?>)) result.value.add(notify.getItem());
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
		assertArrayEquals(RESPONSE.data, result.value.toArray());
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
				jsonDown.getBytes(),
				jsonUp.getBytes()
		};
		final LunarMQServerStub mqServer    = new LunarMQServerStub(MQ_RESPONSES);
		final LunarUrlData.Response RESPONSE = new LunarUrlData().new Response();
		RESPONSE.data = new LunarUrlData();
		RESPONSE.data.url = String.format("localhost:%d/sourceUpdates", mqServer.startServer());
		final byte[][] HTTP_RESPONSES = new byte[][]{
			object2JsonString(LunarUrlData.Response.class).call(RESPONSE).getBytes()
  	    };
		final HttpServerStub lunarServer                     = new HttpServerStub(HTTP_RESPONSES);
		final Lunar          lunar                           = new Lunar(LUNAR_HOST,lunarServer.startServer(),DEVELOPER_ID);
		final ObjectHolder<Throwable> error                  = new ObjectHolder<Throwable>();
		final Map<Integer, LunarSource> map = new HashMap<Integer, LunarSource>();
		
		//TODO: non-trivial logic. Need to leverage it somewhere (plugins?)
		lunar.getStatusUpdatesStream("sources", LunarSource.StatusUpdateMessage.class, LunarSource.class).subscribe(
				new Action1<LunarNotify<LunarSource>>() {
					@Override
					public void call(final LunarNotify<LunarSource> notify) {
						final LunarSource source = notify.getItem();
						if(notify instanceof LunarAdd<?>) {
							if(map.containsKey(source.sourceID) && null==map.get(source.sourceID))
								map.remove(source.sourceID);
							else
								map.put(source.sourceID, source);
						} else if(notify instanceof LunarRemove<?>)
							if(map.containsKey(source.sourceID) && null!=map.get(source.sourceID))
								map.remove(source.sourceID);
							else
								map.put(source.sourceID, null);
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
}
