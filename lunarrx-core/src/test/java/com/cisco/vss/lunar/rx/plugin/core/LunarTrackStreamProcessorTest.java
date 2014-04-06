package com.cisco.vss.lunar.rx.plugin.core;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;
import rx.Observable;
import rx.Subscription;

@RunWith(MockitoJUnitRunner.class)
public class LunarTrackStreamProcessorTest {
	@Mock
	private LunarPluginStateReporter   reporter;
	@Mock
	private LunarMQWriter              writer;
	private Map<Integer, Subscription> tracks;
	private LunarTrack                 resultTrack;
	
	@Before
	public void setUp() {
		tracks      = new HashMap<Integer, Subscription>();
		resultTrack = new LunarTrack(1, "pluginA", "trackB");
	}
	
	@Test
	public void testCall_OK() {
		final byte[]                    BUFFER    = "abcedefg".getBytes();
		final byte[][]                  RESULTS   = new byte[][] {BUFFER};
		final Observable<byte[]>        result    = Observable.from(RESULTS);
		final LunarTrackStreamProcessor processor = new LunarTrackStreamProcessor(reporter, tracks, resultTrack, result);
		
		processor.call(writer);
		
		assertEquals(1, tracks.size());
		assertTrue(tracks.containsKey(1));
		verify(writer).call(BUFFER);
		verify(reporter).running(resultTrack);
		verify(reporter).stopped(resultTrack);
	}

	@Test
	public void testCall_ERROR() {
		final Exception                 ERROR     = new Exception("Error Message");
		final Observable<byte[]>        result    = Observable.error(ERROR);
		final LunarTrackStreamProcessor processor = new LunarTrackStreamProcessor(reporter, tracks, resultTrack, result);
		
		processor.call(writer);
		
		assertEquals(1, tracks.size());
		assertTrue(tracks.containsKey(1));
		verify(reporter).running(resultTrack);
		verify(reporter).stopped(resultTrack, ERROR);
		verify(writer, never()).call((byte[])anyObject());
	}
	
}
