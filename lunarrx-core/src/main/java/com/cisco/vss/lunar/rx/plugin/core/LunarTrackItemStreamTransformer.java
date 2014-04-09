package com.cisco.vss.lunar.rx.plugin.core;

import rx.Observable;
import static com.cisco.vss.rx.java.Conversions.*;
import static com.cisco.vss.lunar.rx.plugin.core.LunarTrackTemplateFactory.*;

public abstract class LunarTrackItemStreamTransformer<T extends TrackItem, R extends TrackItem> extends LunarTrackItemStreamGenerator<R> {
	final Class<T> inputType;
	
	private LunarTrackItemStreamTransformer(final Lunar lunar, final Class<T> inputType, final Class<R> resultType) {
		super(lunar, getTrackTemplate(inputType), resultType);
		this.inputType = inputType;
	}

	@SuppressWarnings("unchecked")
	protected LunarTrackItemStreamTransformer(final Lunar lunar) {
		this(lunar, 
			(Class<T>)getGenericParameterType(0, LunarTrackItemStreamTransformer.class), 
			(Class<R>)getGenericParameterType(1, LunarTrackItemStreamTransformer.class));
	}
	
	@Override
	protected Observable<? extends R> generateR(final Observable<byte[]> input) {
		return transformT(
			input.map(byte2String).flatMap(jsonString2Object(inputType))
		);
	}
	
	protected abstract Observable<? extends R> transformT(final Observable<T> input);
}
