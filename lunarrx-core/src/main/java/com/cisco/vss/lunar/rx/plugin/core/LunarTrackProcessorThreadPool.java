package com.cisco.vss.lunar.rx.plugin.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class LunarTrackProcessorThreadPool {
	private final static Logger                                           LOGGER = LogManager.getLogger();
	private final Lunar                                                   lunar;
	private final Func2<Observable<byte[]>, LunarTrack, Observable<? extends byte[]>> transform;
	
	public LunarTrackProcessorThreadPool(final Lunar lunar, final Func2<Observable<byte[]>, LunarTrack, Observable<? extends byte[]>> transform) {
		this.lunar     = lunar;
		this.transform = transform;
	}
	
	public Subscription startTrack(final LunarTrack sourceTrack, final LunarTrack resultTrack, final Action0 finallyDo) {
		final Observable<? extends byte[]> result = getTransformedStream(sourceTrack, resultTrack); 
		final Observable<LunarMQWriter>    output = lunar.getOutputTrackStream(resultTrack);

		lunar.starting(resultTrack);
		
		return output
		.subscribeOn(Schedulers.newThread()) //TODO: quazar
		.observeOn(Schedulers.trampoline())
		.finallyDo(finallyDo)
		.subscribe(
			new LunarTrackStreamProcessor(lunar, resultTrack, result),
			new Action1<Throwable>(){
				@Override
				public void call(Throwable err) {
					lunar.stopped(resultTrack, err);
					LOGGER.error("Failed to acquire MQ Writer for {}", resultTrack, err.fillInStackTrace());
				}
			}
		);	
	}
	
	private Observable<? extends byte[]> getTransformedStream(final LunarTrack sourceTrack, final LunarTrack resultTrack) {
		return transform.call(lunar.getInputTrackStream(sourceTrack), resultTrack);
	}

	public void stopTrack(final LunarTrack sourceTrack, final LunarTrack resultTrack) {
//		final Integer id = sourceTrack.sourceID;
//		
//		this.reporter.stopping(sourceTrack);
//		this.tracks.get(id).unsubscribe();
//		this.tracks.remove(id);
	}
}
