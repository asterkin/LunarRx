package com.cisco.vss.lunar.rx.plugin.core;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.cisco.vss.lunar.rx.mq.LunarMQWriter;

import static org.mockito.Mockito.*;
import rx.Observable;

@RunWith(MockitoJUnitRunner.class)
public class LunarTrackStreamProcessorTest {
	private static final LunarTrack RESULT_TRACK = new LunarTrack(1, "pluginA", "trackB");
	private static final byte[]                    BUFFER    = "abcedefg".getBytes();
	private static final byte[][]                  RESULTS   = new byte[][] {BUFFER};
	private static final Observable<byte[]>        RESULT    = Observable.from(RESULTS);
	
	@Mock
	private Lunar          lunar;
	@Mock
	private LunarMQWriter  writer;
	
	@Before
	public void setUp() {
	}
	
	@Test
	public void testCall_OK() throws Throwable {
		final LunarTrackStreamProcessor processor = new LunarTrackStreamProcessor(lunar, RESULT_TRACK, RESULT);
		
		when(writer.call(BUFFER)).thenReturn(Observable.from(BUFFER));
		processor.call(writer);
		
		verify(writer).call(BUFFER);
		verify(writer).close();
		verify(lunar).running(RESULT_TRACK);
		verify(lunar).stopped(RESULT_TRACK);
		verify(lunar, never()).stopped(eq(RESULT_TRACK), (Throwable)anyObject());
	}

	@Test
	public void testCall_ErrorOnWrite() throws Throwable {
		final Exception                 ERROR     = new Exception("Error on write");
		final Observable<byte[]>        ERROR_OBS = Observable.error(ERROR); 
		final LunarTrackStreamProcessor processor = new LunarTrackStreamProcessor(lunar, RESULT_TRACK, RESULT);
		
		when(writer.call(BUFFER)).thenReturn(ERROR_OBS);
		processor.call(writer);
		
		verify(lunar).running(RESULT_TRACK);
		verify(lunar).stopped(RESULT_TRACK, ERROR);
		verify(lunar, never()).stopped(RESULT_TRACK);
	}

	@Test
	public void testCall_ErrorFromStream() {
		final Exception                 ERROR     = new Exception("Error from stream");
		final Observable<byte[]>        result    = Observable.error(ERROR);
		final LunarTrackStreamProcessor processor = new LunarTrackStreamProcessor(lunar, RESULT_TRACK, result);
		
		processor.call(writer);
		
		verify(lunar).running(RESULT_TRACK);
		verify(lunar).stopped(RESULT_TRACK, ERROR);
		verify(writer, never()).call((byte[])anyObject());
	}
	
	@Test
	public void testCall_ErrorOnClose() throws IOException {
		final LunarTrackStreamProcessor processor = new LunarTrackStreamProcessor(lunar, RESULT_TRACK, RESULT);
		
		when(writer.call(BUFFER)).thenReturn(Observable.from(BUFFER));
		doThrow(new IOException("Error on close")).when(writer).close();
		processor.call(writer);
		
		verify(writer).call(BUFFER);
		verify(lunar).running(RESULT_TRACK);
		verify(lunar).stopped(RESULT_TRACK);
		verify(lunar, never()).stopped(eq(RESULT_TRACK), (Throwable)anyObject());
	}	
}
