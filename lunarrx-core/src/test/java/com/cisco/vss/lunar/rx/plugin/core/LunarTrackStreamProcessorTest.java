package com.cisco.vss.lunar.rx.plugin.core;

import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;
import rx.Observable;

@RunWith(MockitoJUnitRunner.class)
public class LunarTrackStreamProcessorTest {
	@Mock
	private Lunar          lunar;
	@Mock
	private LunarMQWriter  writer;
	private LunarTrack     resultTrack;
	
	@Before
	public void setUp() {
		resultTrack = new LunarTrack(1, "pluginA", "trackB");
	}
	
	@Test
	public void testCall_OK() throws IOException {
		final byte[]                    BUFFER    = "abcedefg".getBytes();
		final byte[][]                  RESULTS   = new byte[][] {BUFFER};
		final Observable<byte[]>        result    = Observable.from(RESULTS);
		final LunarTrackStreamProcessor processor = new LunarTrackStreamProcessor(lunar, resultTrack, result);
		
		processor.call(writer);
		
		verify(writer).call(BUFFER);
		verify(writer).close();
		verify(lunar).running(resultTrack);
		verify(lunar).stopped(resultTrack);
	}

	@Test
	public void testCall_ERROR() {
		final Exception                 ERROR     = new Exception("Error Message");
		final Observable<byte[]>        result    = Observable.error(ERROR);
		final LunarTrackStreamProcessor processor = new LunarTrackStreamProcessor(lunar, resultTrack, result);
		
		processor.call(writer);
		
		verify(lunar).running(resultTrack);
		verify(lunar).stopped(resultTrack, ERROR);
		verify(writer, never()).call((byte[])anyObject());
	}

	@Ignore //Nothing to assert, just need to look at logs printed out
	@Test
	public void testCall_IOException() throws IOException {
		final byte[]                    BUFFER    = "abcedefg".getBytes();
		final byte[][]                  RESULTS   = new byte[][] {BUFFER};
		final Observable<byte[]>        result    = Observable.from(RESULTS);
		final LunarTrackStreamProcessor processor = new LunarTrackStreamProcessor(lunar, resultTrack, result);
		
		doThrow(new IOException("Close failed")).when(writer).close();
		processor.call(writer);
	}
	
}
