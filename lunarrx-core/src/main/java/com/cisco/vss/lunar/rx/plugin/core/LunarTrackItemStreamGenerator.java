package com.cisco.vss.lunar.rx.plugin.core;

import rx.Observable;
import static com.cisco.vss.rx.java.Conversions.*;

public abstract class LunarTrackItemStreamGenerator<R extends TrackItem> extends LunarByteStreamTransformer {
	final Class<R> resultType;

	protected LunarTrackItemStreamGenerator(final Lunar lunar, final String developerID, final Class<R> resultType) {
		super(lunar, developerID);
		this.resultType = resultType;
	}

	@Override
	protected Observable<byte[]> transform(final Observable<byte[]> input) {
		return transform(resultType, input)
			   .map(object2JsonString(resultType))
			   .map(string2Byte);
	}
	
	protected abstract Observable<R> transform(Class<R> clazz, final Observable<byte[]> input);

}
