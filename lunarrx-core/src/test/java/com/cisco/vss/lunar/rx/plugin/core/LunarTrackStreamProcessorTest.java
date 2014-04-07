package com.cisco.vss.lunar.rx.plugin.core;

import java.io.IOException;

import org.junit.Before;
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
	public void testCall_OK() throws Throwable {
		final byte[]                    BUFFER    = "abcedefg".getBytes();
		final byte[][]                  RESULTS   = new byte[][] {BUFFER};
		final Observable<byte[]>        result    = Observable.from(RESULTS);
		final LunarTrackStreamProcessor processor = new LunarTrackStreamProcessor(lunar, resultTrack, result);
		
		when(writer.call(BUFFER)).thenReturn(Observable.from(BUFFER));
		processor.call(writer);
		
		verify(writer).call(BUFFER);
		verify(writer).close();
		verify(lunar).running(resultTrack);
		verify(lunar).stopped(resultTrack);
		verify(lunar, never()).stopped(eq(resultTrack), (Throwable)anyObject());
	}

	@Test
	public void testCall_ErrorOnWrite() throws Throwable {
		final byte[]                    BUFFER    = "abcedefg".getBytes();
		final byte[][]                  RESULTS   = new byte[][] {BUFFER};
		final Observable<byte[]>        result    = Observable.from(RESULTS);
		final Exception                 ERROR     = new Exception("Error on write");
		final Observable<byte[]>        ERROR_OBS = Observable.error(ERROR); 
		final LunarTrackStreamProcessor processor = new LunarTrackStreamProcessor(lunar, resultTrack, result);
		
		when(writer.call(BUFFER)).thenReturn(ERROR_OBS);
		processor.call(writer);
		
		verify(lunar).running(resultTrack);
		verify(lunar).stopped(resultTrack, ERROR);
		verify(lunar, never()).stopped(resultTrack);
	}

	@Test
	public void testCall_ErrorFromStream() {
		final Exception                 ERROR     = new Exception("Error from stream");
		final Observable<byte[]>        result    = Observable.error(ERROR);
		final LunarTrackStreamProcessor processor = new LunarTrackStreamProcessor(lunar, resultTrack, result);
		
		processor.call(writer);
		
		verify(lunar).running(resultTrack);
		verify(lunar).stopped(resultTrack, ERROR);
		verify(writer, never()).call((byte[])anyObject());
	}
	
	@Test
	public void testCall_ErrorOnClose() throws IOException {
		final byte[]                    BUFFER    = "abcedefg".getBytes();
		final byte[][]                  RESULTS   = new byte[][] {BUFFER};
		final Observable<byte[]>        result    = Observable.from(RESULTS);
		final LunarTrackStreamProcessor processor = new LunarTrackStreamProcessor(lunar, resultTrack, result);
		
		when(writer.call(BUFFER)).thenReturn(Observable.from(BUFFER));
		doThrow(new IOException("Error on close")).when(writer).close();
		processor.call(writer);
		
		verify(writer).call(BUFFER);
		verify(lunar).running(resultTrack);
		verify(lunar).stopped(resultTrack);
		verify(lunar, never()).stopped(eq(resultTrack), (Throwable)anyObject());
	}	
}
