package com.cisco.vss.lunar.rx.plugin.core;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.ArgumentMatcher;
import org.mockito.runners.MockitoJUnitRunner;

import com.cisco.vss.lunar.rx.mq.LunarMQWriter;

import static org.mockito.Mockito.*;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func2;

@RunWith(MockitoJUnitRunner.class)
public class LunarByteStreamTransformerTest {
	private static final LunarTrack              SOURCE_TRACK_TEMPLATE = new LunarTrack(null, "pluginA", "trackB");
	private static final LunarTrack              RESULT_TRACK_TEMPLATE = new LunarTrack(null, "pluginX", "trackY");
	private static final byte[]                  INPUT                 = "abced".getBytes();
	private static final byte[]                  RESULT                = "xyz".getBytes();
	private static final Observable<byte[]>      INPUT_STREAM          = Observable.from(INPUT);
	private static final Observable<byte[]>      RESULT_STREAM         = Observable.from(RESULT);
	private static final LunarTrack              INPUT_TRACK           = new LunarTrack(1, "pluginA", "trackB");
	private static final LunarTrack              RESULT_TRACK          = new LunarTrack(1, "pluginX", "trackY");
	private static final LunarNotify<LunarTrack> NOTIFY                = new LunarAdd<LunarTrack>(INPUT_TRACK);
	private static final Func2<Observable<byte[]>, LunarTrack, Observable<? extends byte[]>> TRANSFORM = new Func2<Observable<byte[]>, LunarTrack, Observable<? extends byte[]>>() {
		@Override
		public Observable<? extends byte[]> call(final Observable<byte[]> source, final	LunarTrack tract) {
			return RESULT_STREAM;
		}			
	};

	@Mock private Lunar                lunar;
	@Mock private LunarMQWriter        writer;
	private LunarByteStreamTransformer transformer;
	
	//TODO: re-factor this mess!!
	@Before
	public void setUp() {
		transformer = new LunarByteStreamTransformer(lunar, SOURCE_TRACK_TEMPLATE, TRANSFORM, RESULT_TRACK_TEMPLATE);
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
		final Observable<LunarNotify<LunarTrack>> NOTIFY_STREAM = Observable.create(new Observable.OnSubscribe<LunarNotify<LunarTrack>>() {
			@Override
			public void call(final Subscriber<? super LunarNotify<LunarTrack>> subscriber) {
				subscriber.onNext(NOTIFY);
				while(transformer.getNumberOfActiveTracks() > 0)
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}				
			}});
		
		when(lunar.getTracks(SOURCE_TRACK_TEMPLATE)).thenReturn(NOTIFY_STREAM);
		when(lunar.getInputTrackStream(INPUT_TRACK)).thenReturn(INPUT_STREAM); 
		when(lunar.getOutputTrackStream(argThat(new TrackMatcher(RESULT_TRACK)))).thenReturn(Observable.from(writer));
		when(writer.call(RESULT)).thenReturn(Observable.from(RESULT));
		
		transformer.run();
		
		verify(lunar).starting(argThat(new TrackMatcher(RESULT_TRACK)));
		verify(lunar).running(argThat(new TrackMatcher(RESULT_TRACK)));
		verify(lunar).stopped(argThat(new TrackMatcher(RESULT_TRACK)));
	}
	
	@Ignore //stop does not work properly yet
	@Test
	public void testStopTrack() {
		final LunarNotify<LunarTrack>             NOTIFY_UP     = new LunarAdd<LunarTrack>(INPUT_TRACK);
		final LunarNotify<LunarTrack>             NOTIFY_DOWN   = new LunarRemove<LunarTrack>(INPUT_TRACK);
		final Observable<LunarNotify<LunarTrack>> NOTIFY_STREAM = Observable.create(new Observable.OnSubscribe<LunarNotify<LunarTrack>>() {
			@Override
			public void call(final Subscriber<? super LunarNotify<LunarTrack>> subscriber) {
				subscriber.onNext(NOTIFY_UP);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				subscriber.onNext(NOTIFY_DOWN);
				while(transformer.getNumberOfActiveTracks() > 0)
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}				
			}});
		
		when(lunar.getTracks(SOURCE_TRACK_TEMPLATE)).thenReturn(NOTIFY_STREAM);
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
