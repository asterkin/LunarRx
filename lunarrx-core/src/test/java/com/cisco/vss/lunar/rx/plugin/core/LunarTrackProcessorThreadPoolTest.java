package com.cisco.vss.lunar.rx.plugin.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;
import rx.Observable;
import rx.functions.Func1;

@RunWith(MockitoJUnitRunner.class)
public class LunarTrackProcessorThreadPoolTest {
	@Mock
	private Lunar                                         lunar;
	@Mock
	private Func1<Observable<byte[]>, Observable<byte[]>> transform;
	@Mock
	private LunarMQWriter                                 writer;
	private LunarTrack                                    sourceTrack;
	private LunarTrack                                    resultTrack;

	@Before
	public void setUp() {
		sourceTrack   = new LunarTrack(1, "pluginA", "trackB");
		resultTrack   = new LunarTrack(1, "pluginX", "trackY");
	}
	
	@Test
	public void testStartTrack_OK() {
		final String                        INPUT         = "abced";
		final String                        RESULT        = "xyz";
		final Observable<byte[]>            INPUT_STREAM  = Observable.from(new byte[][]{INPUT.getBytes()});
		final Observable<byte[]>            RESULT_STREAM = Observable.from(new byte[][]{RESULT.getBytes()});
		final LunarTrackProcessorThreadPool pool          = new LunarTrackProcessorThreadPool(lunar, transform );
		
		when(lunar.getOutputTrackStream(resultTrack)).thenReturn(Observable.from(writer));
		when(lunar.getInputTrackStream(sourceTrack)).thenReturn(INPUT_STREAM); //TODO: use LunarTrack in Lunar API?
		when(transform.call(INPUT_STREAM)).thenReturn(RESULT_STREAM);
		
		pool.startTrack(sourceTrack, resultTrack);
		
		verify(lunar).starting(resultTrack);
		verify(lunar).running(resultTrack);
		verify(lunar).stopped(resultTrack);
	}

}
