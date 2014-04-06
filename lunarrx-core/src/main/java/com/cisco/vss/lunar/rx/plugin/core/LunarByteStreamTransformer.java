package com.cisco.vss.lunar.rx.plugin.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

public abstract class LunarByteStreamTransformer {
	protected final Lunar                         lunar;
	protected final Logger                        logger;
	private   final LunarTrack                    sourceTemplate;
	private   final LunarTrackProcessorThreadPool threadPool;

	protected  LunarByteStreamTransformer(final Lunar lunar, final LunarTrack sourceTemplate, final LunarTrack resultTemplate) {
		this.lunar          = lunar;
		this.logger         = LogManager.getLogger();
		this.sourceTemplate = sourceTemplate;
		this.threadPool = new LunarTrackProcessorThreadPool(lunar, new Func1<Observable<byte[]>, Observable<byte[]>>(){

			@Override
			public Observable<byte[]> call(final Observable<byte[]> inputStream) {
				return transform(inputStream);
			}}, resultTemplate);
	}

	public void run() {
		//TODO: re-start
		lunar.getTracks(this.sourceTemplate)
		.subscribe(
				new Action1<LunarNotify<LunarTrack>>() {
					@Override
					public void call(final LunarNotify<LunarTrack> notify) {
						reflectTrackStatus(notify);
					}
				},
				new Action1<Throwable>() {
					@Override
					public void call(final Throwable err) {
						logger.fatal("Got an error while getting Tracks status", err);
					}
	
				},
				new Action0() {
					@Override
					public void call() {
						logger.warn("Unexpected end of Tracks status update stream. Is Lunar up?");
					}					
				}			
		);
	}

	private void reflectTrackStatus(final LunarNotify<LunarTrack> notify) {
		final LunarTrack track = notify.getItem();
		if(notify instanceof LunarAdd<?>) threadPool.startTrack(track);
		else if (notify instanceof LunarRemove<?>) threadPool.stopTrack(track);
	}

	protected abstract Observable<byte[]> transform(final Observable<byte[]> input);
}
