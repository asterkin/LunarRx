package com.cisco.vss.lunar.rx.plugin.core;

import rx.Observable;
import static com.cisco.vss.lunar.rx.plugin.core.LunarConversions.*;
import rx.functions.Func2;
import rx.functions.Func1;

public class LunarTrackItemStreamGenerator<R extends LunarTrackItem> implements Func2<Observable<byte[]>, LunarTrack, Observable<? extends byte[]>> {
	protected final Class<R>                                           resultType;
	private   final Func1<Observable<byte[]>, Observable<? extends R>> transform;

	protected LunarTrackItemStreamGenerator(final Class<R> resultType, final Func1<Observable<byte[]>, Observable<? extends R>> transform) {
		this.resultType = resultType;
		this.transform  = transform;
	}

	@Override
	public Observable<byte[]> call(final Observable<byte[]> input, final LunarTrack resultTrack) {
		return this.transform.call(input)
			   .map(setTrackDetails(resultTrack, resultType))
			   .map(object2JsonString(resultType))
			   .map(string2Byte);
	}
}
