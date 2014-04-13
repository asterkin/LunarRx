package com.cisco.vss.lunar.rx.plugin.core;

import rx.Observable;
import static com.cisco.vss.lunar.rx.plugin.core.LunarConversions.*;
import static com.cisco.vss.lunar.rx.plugin.core.LunarTrackTemplateFactory.*;

public abstract class LunarTrackItemStreamGenerator<R extends LunarTrackItem> extends LunarByteStreamTransformer {
	protected final Class<R> resultType;

	protected LunarTrackItemStreamGenerator(final Lunar lunar, final LunarTrack sourceTemplate, Class<R> resultType) {
		super(lunar, sourceTemplate, getTrackTemplate(resultType));
		this.resultType = resultType;
	}

	@Override
	protected Observable<byte[]> transform(final Observable<byte[]> input, final LunarTrack resultTrack) {
		return generateR(input)
			   .map(setTrackDetails(resultTrack, resultType))
			   .map(object2JsonString(resultType))
			   .map(string2Byte);
	}
	
	protected abstract Observable<? extends R> generateR(final Observable<byte[]> input);

}
