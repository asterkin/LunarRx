package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.rx.java.Conversions.*;
import static org.junit.Assert.*;


import org.junit.Test;

import com.cisco.vss.rx.java.ObjectHolder;

import rx.Observable;
import rx.functions.Func1;

public class LunarTrackItemStreamGeneratorTest {
	private class TestTrackItem extends LunarTrackItem {
		public final String data;
		
		public TestTrackItem(final String data) {
			this.data = data;
		}
	};
	
	private static final String                       DATA        = "1234567";
	private static final int                          SOURCE_ID   = 987;
	private static final String                       PLUGIN_NAME = "test_plugin";
	private static final String                       TRACK_NAME  = "test_track";
	private static final String                       INPUT       = "abcdefg";
	private static final Observable<byte[]>           INPUT_OBS   = Observable.from(INPUT.getBytes());
	private static final LunarTrack                   TRACK       = new LunarTrack(SOURCE_ID, PLUGIN_NAME, TRACK_NAME);
	private final        TestTrackItem                ITEM        = new TestTrackItem(DATA);
	private final Observable<? extends TestTrackItem> RESULT_OBS  = Observable.from(ITEM);

	//
	//Mockito generates a compiler error on theReturn for RESULT_OBS, hence proprietary
	//
	@Test
	public void testCall() {
		final ObjectHolder<Observable<byte []>> input  = new ObjectHolder<Observable<byte []>>(); 
		
		final Func1<Observable<byte[]>, Observable<? extends TestTrackItem>> transform = new Func1<Observable<byte[]>, Observable<? extends TestTrackItem>>() {
			@Override
			public Observable<? extends TestTrackItem> call(final Observable<byte[]> t1) {
				input.value = t1;
				return RESULT_OBS;
			}};

		final LunarTrackItemStreamGenerator<TestTrackItem> generator = new LunarTrackItemStreamGenerator<TestTrackItem>(TestTrackItem.class, transform);
		
		final byte[]        res    = generator.call(INPUT_OBS, TRACK).toBlockingObservable().last();
		final TestTrackItem output = jsonString2Object(TestTrackItem.class).call(new String(res)).toBlockingObservable().last();
		
		assertEquals(INPUT_OBS,   input.value);
		assertEquals(SOURCE_ID,   output.sourceID);
		assertEquals(PLUGIN_NAME, output.pluginName);
		assertEquals(TRACK_NAME,  output.trackName);
		assertEquals(DATA,        output.data);
	}
}
