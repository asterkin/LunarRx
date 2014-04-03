package com.cisco.vss.lunar.rx.plugin.core;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public abstract class LunarTrackProcessor implements Action1<LunarNotify<LunarTrack>>{
	private final Lunar                      lunar;
	private final LunarPluginStateReporter   reporter;
	private final String                     developerID;
	private final Map<Integer, Subscription> tracks;
	
	public LunarTrackProcessor(final Lunar lunar, final String developerID) {
		this.lunar       = lunar;
		this.reporter    = new LunarPluginStateReporter(lunar, developerID);
		this.developerID = developerID;
		this.tracks      = new HashMap<Integer, Subscription>();		
	}
	
	@Override
	public void call(final LunarNotify<LunarTrack> notify) {
		final LunarTrack track = notify.getItem();
		if(notify instanceof LunarAdd<?>) startTrack(track);
		else if (notify instanceof LunarRemove<?>) stopTrack(track);
	}

	private void stopTrack(final LunarTrack track) {
		final Integer id = track.sourceID;
		
		this.reporter.stopping(track);
		this.tracks.get(id).unsubscribe();
		this.tracks.remove(id);
	}

	private void startTrack(final LunarTrack inputTrack) {
		final Observable<byte[]>        result      = getResultStream(inputTrack);
		final LunarTrack                resultTrack = getResultTrackTemplate(inputTrack.sourceID);
		final Observable<LunarMQWriter> output      = lunar.getOutputTrackStream(developerID, resultTrack);

		reporter.starting(resultTrack);
		
		output
		.subscribeOn(Schedulers.newThread()) //TODO: quazar
		.observeOn(Schedulers.trampoline())
		.subscribe(
			new Action1<LunarMQWriter>(){
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
					tracks.put(inputTrack.sourceID, sub);
				}				
			}, 
			new Action1<Throwable>(){
				@Override
				public void call(Throwable err) {
					reporter.stopped(resultTrack, err);
				}
			}
		);		
	}
	
	private Observable<byte[]> getResultStream(final LunarTrack track) {
		return transform(track.getBytestream());
	}
	
	protected abstract LunarTrack         getResultTrackTemplate(final Integer sourceID);
	protected abstract Observable<byte[]> transform(final Observable<byte[]> input);		
}

