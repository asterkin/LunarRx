package com.cisco.vss.lunar.rx.plugin.core;

import rx.Observable;
import static com.cisco.vss.rx.java.Conversions.*;

public abstract class LunarTrackItemStreamGenerator<R extends TrackItem> extends LunarByteStreamTransformer {
	final Class<R> resultType;

	protected LunarTrackItemStreamGenerator(final Lunar lunar, final String developerID, final Class<R> resultType) {
		super(lunar, developerID, null, null, null, null); //TODO: detect automatically
		this.resultType = resultType;
	}

	@Override
	protected Observable<byte[]> transform(final Observable<byte[]> input) {
		return generateR(input)
			   .map(object2JsonString(resultType))
			   .map(string2Byte);
	}
	
	protected abstract Observable<R> generateR(final Observable<byte[]> input);

}
