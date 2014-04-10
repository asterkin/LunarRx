package com.cisco.vss.lunar.rx.plugin.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import com.cisco.vss.rx.java.ObjectHolder;
import static org.mockito.Mockito.*;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func2;

@RunWith(MockitoJUnitRunner.class)
public class LunarTrackProcessorThreadPoolTest {
	@Mock
	private Lunar                                                   lunar;
	@Mock
	private LunarMQWriter                                           writer;
	private final byte[]                        RESULT        = "xyz".getBytes();
	private final Observable<? extends byte[]>  RESULT_STREAM = Observable.from(new byte[][]{RESULT});
	private Func2<Observable<byte[]>, LunarTrack, Observable<? extends byte[]>> transform = new Func2<Observable<byte[]>, LunarTrack, Observable<? extends byte[]>>() {
		@Override
		public Observable<? extends byte[]> call(final Observable<byte[]> t1, final LunarTrack track) {
			return RESULT_STREAM;
		}
		
	};
	private LunarTrack                                              sourceTrack;
	private LunarTrack                                              resultTrack;

	@Before
	public void setUp() {
		sourceTrack   = new LunarTrack(1, "pluginA", "trackB");
		resultTrack   = new LunarTrack(1, "pluginX", "trackY");
	}
	
	@Test
	public void testStartTrack_OK() {
		final ObjectHolder<Object>          lock          = new ObjectHolder<Object>(new Object());
		final Action0                       finallyDo     = new Action0(){
			@Override
			public void call() {
				synchronized(lock) { lock.value = null; }
			}
		};
		final byte[]                        INPUT         = "abced".getBytes();
		final Observable<byte[]>            INPUT_STREAM  = Observable.from(new byte[][]{INPUT});
		final LunarTrackProcessorThreadPool pool          = new LunarTrackProcessorThreadPool(lunar, transform);
		
		when(lunar.getInputTrackStream(sourceTrack)).thenReturn(INPUT_STREAM); 
		when(lunar.getOutputTrackStream(resultTrack)).thenReturn(Observable.from(writer));
		when(writer.call(RESULT)).thenReturn(Observable.from(RESULT));
		
		pool.startTrack(sourceTrack, resultTrack, finallyDo);
		
		
		while (null != lock.value)
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		verify(lunar).starting(resultTrack);
		verify(lunar).running(resultTrack);
		verify(lunar).stopped(resultTrack);
	}

	@Test
	public void testStartTrack_ERROR() {
		final ObjectHolder<Object>          lock          = new ObjectHolder<Object>(new Object());
		final Action0                       finallyDo     = new Action0(){
			@Override
			public void call() {
				synchronized(lock) { lock.value = null; }
			}
		};
		final byte[]                        INPUT         = "abced".getBytes();
		final Observable<byte[]>            INPUT_STREAM  = Observable.from(new byte[][]{INPUT});
		final Throwable                     ERROR         = new Exception("Error to acquire Writer");
		final Observable<LunarMQWriter>     ERROR_OBS     = Observable.error(ERROR);
		final LunarTrackProcessorThreadPool pool          = new LunarTrackProcessorThreadPool(lunar, transform);
		
		when(lunar.getInputTrackStream(sourceTrack)).thenReturn(INPUT_STREAM); 
		when(lunar.getOutputTrackStream(resultTrack)).thenReturn(ERROR_OBS);
		
		pool.startTrack(sourceTrack, resultTrack, finallyDo);
		
		
		while (null != lock.value)
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		verify(lunar).starting(resultTrack);
		verify(lunar).stopped(resultTrack, ERROR);
		verify(lunar, never()).stopped(resultTrack);
		verify(writer, never()).call((byte [])anyObject());
	}
	
}
