package com.cisco.vss.lunar.rx.plugin.core;

import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class LunarStreamProcessor implements Action1<LunarMQWriter> {
	private final LunarPluginStateReporter      reporter;
	private final Map<Integer, Subscription>    tracks;
	private final LunarTrack                    resultTrack;
	private final Observable<byte[]>            result;

	public LunarStreamProcessor(final LunarPluginStateReporter reporter, Map<Integer, Subscription> tracks, final LunarTrack resultTrack, final Observable<byte[]> result) {
		this.reporter    = reporter;
		this.tracks      = tracks;
		this.resultTrack = resultTrack;
		this.result      = result;
	}
	
	@Override
	public void call(final LunarMQWriter writer) {
		reporter.running(resultTrack);
		
		final Subscription  sub = result
				.subscribeOn(Schedulers.newThread()) //TODO: quazar
				.subscribe(
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
		tracks.put(resultTrack.sourceID, sub);
	}
}
