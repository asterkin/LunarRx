package com.cisco.vss.lunar.rx.plugin.core;

import rx.Observable;
import static com.cisco.vss.rx.java.Conversions.*;

public abstract class LunarTrackItemStreamTransformer<T extends TrackItem, R extends TrackItem> extends LunarTrackItemStreamGenerator<R> {
	final Class<T> inputType;
	
	protected LunarTrackItemStreamTransformer(final Lunar lunar, final String developerID, final Class<T> inputType, final Class<R> resultType) {
		super(lunar, developerID, resultType);
		this.inputType = inputType;
	}

	@Override
	protected Observable<R> transform(Class<R> clazz, final Observable<byte[]> input) {
		return transform(
			inputType, 
			resultType, 
			input.map(byte2String).flatMap(jsonString2Object(inputType))
		);
	}
	
	protected abstract Observable<R> transform(final Class<T> inputType, final Class<R> resultType, final Observable<T> input);
}