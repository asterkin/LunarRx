package com.cisco.vss.rx.java;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import rx.Observable;
import rx.functions.Action1;
import static com.cisco.vss.rx.java.Conversions.*;

public class ConcurrentHttpServerStubTest {

	@Test
	public void testHttpGet_OK() throws IOException, InterruptedException {
		final String RESPONSE = "abcdefg";
		@SuppressWarnings("serial")
		final Map<String, String> HTTP_RESPONSES = new HashMap<String,String>(){{
			put("/uri1", RESPONSE);
		}};
		final ConcurrentHttpServerStub server = new ConcurrentHttpServerStub(HTTP_RESPONSES);
		final int                      port   = server.startServer();
		final URL                      url    = new URL("http", "localhost", port, "/uri1");
		final ObjectHolder<String>     result = new ObjectHolder<String>();
		final ObjectHolder<Throwable>  error  = new ObjectHolder<Throwable>();
		
		Observable.from(url)
		.flatMap(synchHttpGet)
		.subscribe(
				new Action1<String>() {
					@Override
					public void call(final String response) {
						result.value = response;
					}
				},
				new Action1<Throwable>() {
					@Override
					public void call(Throwable err) {
						error.value = err;
					}
				}
		);
		server.join();
		assertNull(error.value);
		assertEquals(RESPONSE, result.value);
	}

	@Test
	public void testHttpGet_NOT_FOUND() throws IOException, InterruptedException {
		final String RESPONSE = "abcdefg";
		@SuppressWarnings("serial")
		final Map<String, String> HTTP_RESPONSES = new HashMap<String,String>(){{
			put("/uri1", RESPONSE);
		}};
		final ConcurrentHttpServerStub server         = new ConcurrentHttpServerStub(HTTP_RESPONSES);
		final int                      port           = server.startServer();
		final URL                      url            = new URL("http", "localhost", port, "/uri2");
		final ObjectHolder<String>     result         = new ObjectHolder<String>();
		final ObjectHolder<Throwable>  error          = new ObjectHolder<Throwable>();
		
		Observable.from(url)
		.flatMap(synchHttpGet)
		.subscribe(
				new Action1<String>() {
					@Override
					public void call(final String response) {
						result.value = response;
					}
				},
				new Action1<Throwable>() {
					@Override
					public void call(Throwable err) {
						error.value = err;
					}
				}
		);
		server.interrupt();;
		server.join();
		assertNull(result.value);
		assertEquals("Got HTTP error code [404]", error.value.getMessage());
	}

	@Test
	public void testHttpPost_OK() throws IOException, InterruptedException {
		final String RESPONSE = "12345678";
		final String REQUEST  = "abcedfg";
		@SuppressWarnings("serial")
		final Map<String, String> HTTP_RESPONSES = new HashMap<String,String>(){{
			put("/uri1", RESPONSE);
		}};
		final ConcurrentHttpServerStub server = new ConcurrentHttpServerStub(HTTP_RESPONSES);
		final int                      port   = server.startServer();
		final URL                      url    = new URL("http", "localhost", port, "/uri1");
		final ObjectHolder<String>     result = new ObjectHolder<String>();
		final ObjectHolder<Throwable>  error  = new ObjectHolder<Throwable>();
		
		Observable.from(url)
		.flatMap(synchHttpPost(REQUEST))
		.subscribe(
				new Action1<String>() {
					@Override
					public void call(final String response) {
						result.value = response;
					}
				},
				new Action1<Throwable>() {
					@Override
					public void call(Throwable err) {
						error.value = err;
					}
				}
		);
		server.join();
		final Map<String, String> requests = server.getRequests();
		assertNull(error.value);
		assertEquals(RESPONSE, result.value);
		assertEquals(1, requests.size());
		assertArrayEquals(new String[]{REQUEST}, requests.values().toArray());
	}
	
}
