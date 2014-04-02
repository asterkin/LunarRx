package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.lunar.rx.plugin.core.LunarConversions.*;
import java.util.HashMap;
import java.util.Map;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public abstract class LunarByteStreamTransformer {
	protected final Lunar                      lunar;
	protected final String                     developerID;
	protected final Map<Integer, Subscription> tracks;

	protected  LunarByteStreamTransformer(final Lunar lunar, final String developerID) {
		this.lunar       = lunar;
		this.developerID = developerID;
		this.tracks      = new HashMap<Integer, Subscription>();
	}

	public void run() {
		//TODO: re-start
		lunar.getTracks()
		.filter(pluginTrack(getInputTrackTemplate()))
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
						//TODO: report status
					}
	
				},
				new Action0() {
					@Override
					public void call() {
						//TODO: report status
					}					
				}			
		);
	}

	private void reflectTrackStatus(final LunarNotify<LunarTrack> notify) {
		final LunarTrack track = notify.getItem();
		if(notify instanceof LunarAdd<?>) startTrack(track);
		else if (notify instanceof LunarRemove<?>) stopTrack(track);
	}

	private void stopTrack(final LunarTrack track) {
		final Integer id = track.sourceID;
		
		this.tracks.get(id).unsubscribe();
		this.tracks.remove(id);
	}

	void startTrack(final LunarTrack inputTrack) {
		final Observable<byte[]>        result      = getResultStream(inputTrack);
		final LunarTrack                resultTrack = getResultTrackTemplate(inputTrack.sourceID);
		final Observable<LunarMQWriter> output      = lunar.getOutputTrackStream(developerID, resultTrack);

		lunar.sendReport(LunarPluginStateReport.starting(developerID, resultTrack)).subscribe();//TODO: threads? Error handling
		
		output.subscribe( //TODO: Thread
			new Action1<LunarMQWriter>(){
				@Override
				public void call(final LunarMQWriter writer) {
					lunar.sendReport(LunarPluginStateReport.running(developerID, resultTrack)).subscribe(); //TODO: threads? Error handling
					
					final Subscription  sub = result
							.subscribeOn(Schedulers.newThread()) //TODO: quazar
							.subscribe(
									writer,
									new Action1<Throwable>() {
										@Override
										public void call(final Throwable err) {
											lunar.sendReport(LunarPluginStateReport.stopping(developerID, resultTrack)).subscribe();
										}
									},
									new Action0() {
										@Override
										public void call() {
											lunar.sendReport(LunarPluginStateReport.stopping(developerID, resultTrack)).subscribe();
										}					
									}			
							);
					tracks.put(inputTrack.sourceID, sub);
				}				
			}, 
			new Action1<Throwable>(){
				@Override
				public void call(Throwable t1) {
					lunar.sendReport(LunarPluginStateReport.stopped(developerID, resultTrack)).subscribe();
				}
			},
			new Action0() {
				@Override
				public void call() {
					lunar.sendReport(LunarPluginStateReport.stopped(developerID, resultTrack)).subscribe();
				}				
			}
		);		
	}

	protected abstract LunarTrack         getInputTrackTemplate();
	protected abstract LunarTrack         getResultTrackTemplate(final Integer sourceID);
	protected abstract Observable<byte[]> transform(final Observable<byte[]> input);
	
	protected Observable<byte[]> getResultStream(final LunarTrack track) {
		return transform(track.getBytestream());
	}
}
