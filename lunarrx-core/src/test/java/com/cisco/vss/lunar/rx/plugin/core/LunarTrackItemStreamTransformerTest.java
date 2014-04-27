package com.cisco.vss.lunar.rx.plugin.core;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cisco.vss.rx.java.ObjectHolder;
import static com.cisco.vss.rx.java.Conversions.*;

import rx.Observable;
import rx.functions.Func1;

public class LunarTrackItemStreamTransformerTest {
	private class TestInputTrackItem extends LunarTrackItem {
		public final String data;
		
		public TestInputTrackItem(final String data) {
			this.data = data;
		}
	};

	private class TestResultTrackItem extends LunarTrackItem {
		public final String data;
		
		public TestResultTrackItem(final String data) {
			this.data = data;
		}
	};
	
	private static final String                          INPUT_DATA  = "1234567";
	private        final TestInputTrackItem              INPUT_ITEM  = new TestInputTrackItem(INPUT_DATA);
	private        final Observable<byte[]>              INPUT_OBS   = Observable.from(
			object2JsonString(TestInputTrackItem.class).call(INPUT_ITEM).getBytes());
	private static final String                          RESULT_DATA = "1234567";
	private        final TestResultTrackItem             RESULT_ITEM = new TestResultTrackItem(RESULT_DATA);
	private        final Observable<TestResultTrackItem> RESULT_OBS  = Observable.from(RESULT_ITEM);

	@Test
	public void testTransform() {
		final ObjectHolder<Observable<TestInputTrackItem>> input = new ObjectHolder<Observable<TestInputTrackItem>>();
		final Func1<Observable<TestInputTrackItem>, Observable<? extends TestResultTrackItem>> transform = 
				new Func1<Observable<TestInputTrackItem>, Observable<? extends TestResultTrackItem>>() {

			@Override
			public Observable<? extends TestResultTrackItem> call(final Observable<TestInputTrackItem> t1) {
				input.value = t1;
				return RESULT_OBS;
			}}; 
		final LunarTrackItemStreamTransformer<TestInputTrackItem, TestResultTrackItem> transformer = 
				new LunarTrackItemStreamTransformer<TestInputTrackItem, TestResultTrackItem>(TestInputTrackItem.class, transform);
		
		final TestResultTrackItem result = transformer.call(INPUT_OBS).toBlockingObservable().last();
		final String inputData = input.value.toBlockingObservable().last().data;
		assertEquals(RESULT_ITEM.data, result.data);
		assertEquals(INPUT_DATA, inputData);
	}

}
