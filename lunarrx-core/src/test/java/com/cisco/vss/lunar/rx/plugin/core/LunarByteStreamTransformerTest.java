package com.cisco.vss.lunar.rx.plugin.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.ArgumentMatcher;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;
import rx.Observable;
import rx.Subscriber;

@RunWith(MockitoJUnitRunner.class)
public class LunarByteStreamTransformerTest {
	@Mock
	private Lunar         lunar;
	@Mock
	private LunarMQWriter writer;
	private LunarTrack    sourceTrackTemplate;
	private LunarTrack    resultTrackTemplate;

	//TODO: re-factor this mess!!
	@Before
	public void setUp() {
		sourceTrackTemplate = new LunarTrack(null, "pluginA", "trackB");
		resultTrackTemplate = new LunarTrack(null, "pluginX", "trackY");
	}
	
	private class TrackMatcher extends ArgumentMatcher<LunarTrack> {
		final private LunarTrack expected;
		
		public TrackMatcher(final LunarTrack expected) {
			this.expected = expected;
		}
		
		@Override
		public boolean matches(final Object argument) {
			final LunarTrack track = (LunarTrack)argument;
			return expected.sourceID.equals(track.sourceID)
				   && expected.pluginName.equals(track.pluginName)
				   && expected.trackName.equals(track.trackName);
		}
		
	}
	
	@Test
	public void testStartTrack() {
		final byte[]                              INPUT         = "abced".getBytes();
		final byte[]                              RESULT        = "xyz".getBytes();
		final Observable<byte[]>                  INPUT_STREAM  = Observable.from(INPUT);
		final Observable<byte[]>                  RESULT_STREAM = Observable.from(RESULT);
		final LunarTrack                          INPUT_TRACK   = new LunarTrack(1, "pluginA", "trackB");
		final LunarTrack                          RESULT_TRACK  = new LunarTrack(1, "pluginX", "trackY");
		final LunarNotify<LunarTrack>             NOTIFY        = new LunarAdd<LunarTrack>(INPUT_TRACK);
		final LunarByteStreamTransformer          transformer   = new LunarByteStreamTransformer(lunar, sourceTrackTemplate, resultTrackTemplate) {
			@Override
			protected Observable<byte[]> transform(final Observable<byte[]> input) {
				return RESULT_STREAM;
			}
		};
		final Observable<LunarNotify<LunarTrack>> NOTIFY_STREAM = Observable.create(new Observable.OnSubscribe<LunarNotify<LunarTrack>>() {
			@Override
			public void call(final Subscriber<? super LunarNotify<LunarTrack>> subscriber) {
				subscriber.onNext(NOTIFY);
				while(transformer.getNumberOfActiveTracks() > 0)
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
			}});
		
		when(lunar.getTracks(sourceTrackTemplate)).thenReturn(NOTIFY_STREAM);
		when(lunar.getInputTrackStream(INPUT_TRACK)).thenReturn(INPUT_STREAM); 
		when(lunar.getOutputTrackStream(argThat(new TrackMatcher(RESULT_TRACK)))).thenReturn(Observable.from(writer));
		when(writer.call(RESULT)).thenReturn(Observable.from(RESULT));
		
		transformer.run();
		
		verify(lunar).starting(argThat(new TrackMatcher(RESULT_TRACK)));
		verify(lunar).running(argThat(new TrackMatcher(RESULT_TRACK)));
		verify(lunar).stopped(argThat(new TrackMatcher(RESULT_TRACK)));
	}
	
	@Test
	public void testStopTrack() {
		final byte[]                              INPUT         = "abced".getBytes();
		final byte[]                              RESULT        = "xyz".getBytes();
		final Observable<byte[]>                  INPUT_STREAM  = Observable.from(INPUT);
		final Observable<byte[]>                  RESULT_STREAM = Observable.from(RESULT);
		final LunarTrack                          INPUT_TRACK   = new LunarTrack(1, "pluginA", "trackB");
		final LunarTrack                          RESULT_TRACK  = new LunarTrack(1, "pluginX", "trackY");
		final LunarNotify<LunarTrack>             NOTIFY_UP     = new LunarAdd<LunarTrack>(INPUT_TRACK);
		final LunarNotify<LunarTrack>             NOTIFY_DOWN   = new LunarRemove<LunarTrack>(INPUT_TRACK);
		final LunarByteStreamTransformer          transformer   = new LunarByteStreamTransformer(lunar, sourceTrackTemplate, resultTrackTemplate) {
			@Override
			protected Observable<byte[]> transform(final Observable<byte[]> input) {
				return RESULT_STREAM;
			}
		};
		final Observable<LunarNotify<LunarTrack>> NOTIFY_STREAM = Observable.create(new Observable.OnSubscribe<LunarNotify<LunarTrack>>() {
			@Override
			public void call(final Subscriber<? super LunarNotify<LunarTrack>> subscriber) {
				subscriber.onNext(NOTIFY_UP);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				subscriber.onNext(NOTIFY_DOWN);
				while(transformer.getNumberOfActiveTracks() > 0)
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
			}});
		
		when(lunar.getTracks(sourceTrackTemplate)).thenReturn(NOTIFY_STREAM);
		when(lunar.getInputTrackStream(INPUT_TRACK)).thenReturn(INPUT_STREAM); 
		when(lunar.getOutputTrackStream(argThat(new TrackMatcher(RESULT_TRACK)))).thenReturn(Observable.from(writer));
		when(writer.call(RESULT)).thenReturn(Observable.from(RESULT));
		
		transformer.run();
		
		verify(lunar).starting(argThat(new TrackMatcher(RESULT_TRACK)));
		verify(lunar).running(argThat(new TrackMatcher(RESULT_TRACK)));
		verify(lunar).stopping(argThat(new TrackMatcher(RESULT_TRACK)));
		verify(lunar).stopped(argThat(new TrackMatcher(RESULT_TRACK)));
	}	
}
