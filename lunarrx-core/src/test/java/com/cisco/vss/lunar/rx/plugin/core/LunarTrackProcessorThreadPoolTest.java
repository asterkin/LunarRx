package com.cisco.vss.lunar.rx.plugin.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.cisco.vss.lunar.rx.mq.LunarMQWriter;
import com.cisco.vss.rx.java.ObjectHolder;

import static org.mockito.Mockito.*;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func2;

@RunWith(MockitoJUnitRunner.class)
public class LunarTrackProcessorThreadPoolTest {
	private static final byte[]                        INPUT         = "abced".getBytes();
	private static final Observable<byte[]>            INPUT_STREAM  = Observable.from(new byte[][]{INPUT});
	private static final byte[]                        RESULT        = "xyz".getBytes();
	private static final Observable<? extends byte[]>  RESULT_STREAM = Observable.from(new byte[][]{RESULT});
	private static final LunarTrack                    SOURCE_TRACK  = new LunarTrack(1, "pluginA", "trackB");
	private static final LunarTrack                    RESULT_TRACK  = new LunarTrack(1, "pluginX", "trackY");	
	private static final Func2<Observable<byte[]>, LunarTrack, Observable<? extends byte[]>> TRANSFORM = new Func2<Observable<byte[]>, LunarTrack, Observable<? extends byte[]>>() {
		@Override
		public Observable<? extends byte[]> call(final Observable<byte[]> t1, final LunarTrack track) {
			return RESULT_STREAM;
		}
		
	};
	private static final ObjectHolder<Object>          LOCK          = new ObjectHolder<Object>();
	private static final Action0                       FINALLY_DO    = new Action0(){
		@Override
		public void call() {
			synchronized(LOCK) { LOCK.value = null; }
		}
	};

	private @Mock Lunar                   lunar;
	private @Mock LunarMQWriter           writer;
	private LunarTrackProcessorThreadPool pool;
	
	@Before
	public void setUp() {
		LOCK.value = new Object();
		pool       = new LunarTrackProcessorThreadPool(lunar, TRANSFORM);
	}
	
	@Test
	public void testStartTrack_OK() {
		when(lunar.getInputTrackStream(SOURCE_TRACK)).thenReturn(INPUT_STREAM); 
		when(lunar.getOutputTrackStream(RESULT_TRACK)).thenReturn(Observable.from(writer));
		when(writer.call(RESULT)).thenReturn(Observable.from(RESULT));
		
		pool.startTrack(SOURCE_TRACK, RESULT_TRACK, FINALLY_DO);				
		waitToComplete();
		
		verify(lunar).starting(RESULT_TRACK);
		verify(lunar).running(RESULT_TRACK);
		verify(lunar).stopped(RESULT_TRACK);
	}

	private void waitToComplete() {
		while (null != LOCK.value)
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	@Test
	public void testStartTrack_ERROR() {
		final Throwable                     ERROR         = new Exception("Error to acquire Writer");
		final Observable<LunarMQWriter>     ERROR_OBS     = Observable.error(ERROR);
		
		when(lunar.getInputTrackStream(SOURCE_TRACK)).thenReturn(INPUT_STREAM); 
		when(lunar.getOutputTrackStream(RESULT_TRACK)).thenReturn(ERROR_OBS);
		
		pool.startTrack(SOURCE_TRACK, RESULT_TRACK, FINALLY_DO);
		waitToComplete();
		
		verify(lunar).starting(RESULT_TRACK);
		verify(lunar).stopped(RESULT_TRACK, ERROR);
		verify(lunar, never()).stopped(RESULT_TRACK);
		verify(writer, never()).call((byte [])anyObject());
	}	
}
