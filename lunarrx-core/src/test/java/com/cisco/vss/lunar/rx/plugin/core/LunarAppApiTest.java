package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.rx.java.Conversions.object2JsonString;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import rx.functions.Action1;

import com.cisco.vss.rx.java.HttpServerStub;
import com.cisco.vss.rx.java.ObjectHolder;

public class LunarAppApiTest {
	private static final String  LUNAR_HOST   = "localhost";
	private static final String  DEVELOPER_ID = "6871c4b35301671668ebf26ae46b6441";

	@Test
	public void testGetSources() throws IOException, InterruptedException {
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
		final ObjectHolder<List<LunarSource>> result      = new ObjectHolder<List<LunarSource>>(new ArrayList<LunarSource>());
		
		lunar.getSources().subscribe(
				new Action1<LunarSource>() {
					@Override
					public void call(final LunarSource source) {
						result.value.add(source);
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
	public void testGetUpdatesUrl() throws IOException, InterruptedException {
		final LunarUrlData.Response RESPONSE = new LunarUrlData().new Response();
		RESPONSE.data = new LunarUrlData();
		RESPONSE.data.url = "54.195.242.59:7030/sourceUpdates";
		final byte[][] HTTP_RESPONSES = new byte[][]{
			object2JsonString(LunarUrlData.Response.class).call(RESPONSE).getBytes()
  	    };
		final HttpServerStub lunarServer    = new HttpServerStub(HTTP_RESPONSES);
		final Lunar          lunar          = new Lunar(LUNAR_HOST,lunarServer.startServer(),DEVELOPER_ID);
		final ObjectHolder<Throwable> error = new ObjectHolder<Throwable>();
		final ObjectHolder<String>    result= new ObjectHolder<String>();
		
		lunar.getUpdatesUrl("sources").subscribe(
				new Action1<String>() {
					@Override
					public void call(final String url) {
						result.value = url;
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
		assertEquals(RESPONSE.data.url, result.value);
	}
}
