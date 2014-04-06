package com.cisco.vss.lunar.rx.plugin.core;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LunarTrackProcessorThreadPool {
	private final Lunar                                         lunar;
	private final LunarPluginStateReporter                      reporter;
	private final String                                        developerID;
	private final Func1<Observable<byte[]>, Observable<byte[]>> transform;
	private final LunarTrack                                    resultTemplate;

	public LunarTrackProcessorThreadPool(final Lunar lunar, final String developerID, final Func1<Observable<byte[]>, Observable<byte[]>> transform, final LunarTrack resultTemplate) {
		this.lunar          = lunar;
		this.developerID    = developerID;
		this.reporter       = new LunarPluginStateReporter(lunar, developerID);//TODO: hide in Lunar
		this.transform      = transform;
		this.resultTemplate = resultTemplate;
	}
	
	public void startTrack(final LunarTrack sourceTrack) {
		final Observable<byte[]>        result      = getTransformedStream(sourceTrack).subscribeOn(Schedulers.newThread()); //TODO: quazar or not required?
		final LunarTrack                resultTrack = resultTemplate.attachToSource(sourceTrack.sourceID);
		final Observable<LunarMQWriter> output      = lunar.getOutputTrackStream(developerID, resultTrack);

		reporter.starting(resultTrack);
		
		output
		.subscribeOn(Schedulers.newThread()) //TODO: quazar
		.observeOn(Schedulers.trampoline())
		.subscribe(
			new LunarTrackStreamProcessor(reporter, resultTrack, result),
			new Action1<Throwable>(){
				@Override
				public void call(Throwable err) {
					reporter.stopped(resultTrack, err);
				}
			}
		);	
	}
	
	private Observable<byte[]> getTransformedStream(final LunarTrack sourceTrack) {
		return transform.call(sourceTrack.getBytestream());
	}

	public void stopTrack(final LunarTrack sourceTrack) {
//		final Integer id = sourceTrack.sourceID;
//		
//		this.reporter.stopping(sourceTrack);
//		this.tracks.get(id).unsubscribe();
//		this.tracks.remove(id);
	}
}
