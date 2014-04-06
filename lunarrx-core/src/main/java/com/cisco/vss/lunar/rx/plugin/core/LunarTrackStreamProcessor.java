package com.cisco.vss.lunar.rx.plugin.core;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

public class LunarTrackStreamProcessor implements Action1<LunarMQWriter> {
	private final LunarPluginStateReporter      reporter;
	private final LunarTrack                    resultTrack;
	private final Observable<byte[]>            result;

	public LunarTrackStreamProcessor(final LunarPluginStateReporter reporter, final LunarTrack resultTrack, final Observable<byte[]> result) {
		this.reporter    = reporter;
		this.resultTrack = resultTrack;
		this.result      = result;
	}
	
	@Override
	public void call(final LunarMQWriter writer) {
		reporter.running(resultTrack);
		
		result.subscribe(
			writer,
			new Action1<Throwable>() {
				@Override
				public void call(final Throwable err) {
					reporter.stopped(resultTrack, err);
				}
			},
			new Action0() {
				@Override
				public void call() {
					reporter.stopped(resultTrack);
				}					
			}			
		);
	}
}
