package com.cisco.vss.lunar.rx.plugin.core;

import rx.Observable;
import static com.cisco.vss.rx.java.Conversions.*;
import rx.functions.Func1;

public class LunarTrackItemStreamTransformer<T extends LunarTrackItem, R extends LunarTrackItem> implements Func1<Observable<byte[]>, Observable<? extends R>> {
	private final Class<T>                                      sourceType;
	private final Func1<Observable<T>, Observable<? extends R>> transform;
	
	protected LunarTrackItemStreamTransformer(final Class<T> sourceType, Func1<Observable<T>, Observable<? extends R>> transform) {
		this.sourceType = sourceType;
		this.transform  = transform;
	}

	@Override
	public Observable<? extends R> call(final Observable<byte[]> input) {
		return this.transform.call(
			input.map(byte2String).flatMap(jsonString2Object(sourceType))
		);
	}
}
