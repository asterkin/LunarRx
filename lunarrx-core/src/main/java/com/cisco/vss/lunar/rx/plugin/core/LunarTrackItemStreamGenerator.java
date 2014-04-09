package com.cisco.vss.lunar.rx.plugin.core;

import java.lang.reflect.ParameterizedType;

import rx.Observable;
import static com.cisco.vss.rx.java.Conversions.*;
import static com.cisco.vss.lunar.rx.plugin.core.LunarTrackTemplateFactory.*;

public abstract class LunarTrackItemStreamGenerator<R extends TrackItem> extends LunarByteStreamTransformer {
	final Class<R> resultType;

	@SuppressWarnings("rawtypes")
	protected static <M> Class getGenericParameterType(int index, Class<M> clazz) {
		return (Class)((ParameterizedType)clazz.getGenericSuperclass()).getActualTypeArguments()[index];
	}

	protected LunarTrackItemStreamGenerator(final Lunar lunar, final LunarTrack sourceTemplate, Class<R> resultType) {
		super(lunar, sourceTemplate, getTrackTemplate(resultType));
		this.resultType = resultType;
	}
	
	@SuppressWarnings("unchecked")
	protected LunarTrackItemStreamGenerator(final Lunar lunar, final LunarTrack sourceTemplate) {
		this(lunar, sourceTemplate, (Class<R>)getGenericParameterType(0, LunarTrackItemStreamGenerator.class));
	}

	@Override
	protected Observable<byte[]> transform(final Observable<byte[]> input) {
		return generateR(input)
			   .map(object2JsonString(resultType))
			   .map(string2Byte);
	}
	
	protected abstract Observable<? extends R> generateR(final Observable<byte[]> input);

}
