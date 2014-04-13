package com.cisco.vss.lunar.rx.plugin.core;

import rx.Observable;
import static com.cisco.vss.rx.java.Conversions.*;
import static com.cisco.vss.lunar.rx.plugin.core.LunarTrackTemplateFactory.*;

public abstract class LunarTrackItemStreamTransformer<T extends LunarTrackItem, R extends LunarTrackItem> extends LunarTrackItemStreamGenerator<R> {
	private final Class<T> sourceType;
	
	protected LunarTrackItemStreamTransformer(final Lunar lunar, final Class<T> sourceType, final Class<R> resultType) {
		super(lunar, getTrackTemplate(sourceType), resultType);
		this.sourceType = sourceType;
	}

	@Override
	protected Observable<? extends R> generateR(final Observable<byte[]> input) {
		return transformT(
			input.map(byte2String).flatMap(jsonString2Object(sourceType))
		);
	}
	
	protected abstract Observable<? extends R> transformT(final Observable<T> input);
}
