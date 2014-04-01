package com.cisco.vss.lunar.rx.plugin.core;

import rx.Observable;
import rx.functions.Func1;

public abstract class LunarTrackItemStreamTransformer<T extends TrackItem, R extends TrackItem> extends LunarByteStreamTransformer {
	final Class<T> inputType;
	final Class<R> resultType;
	
	protected LunarTrackItemStreamTransformer(final Lunar lunar, final String developerID, final Class<T> inputType, final Class<R> resultType) {
		super(lunar, developerID);
		this.inputType = inputType;
		this.resultType  = resultType;
	}
	
	protected abstract Func1<T, Observable<R>> transform();
}
