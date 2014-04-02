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

	private void reportStatus(final LunarPluginStateReport report) {
		lunar.sendReport(report).subscribe();//TODO: threads? Error handling		
	}
	
	private void starting(final LunarTrack track) {
		reportStatus(LunarPluginStateReport.starting(developerID, track));
	}

	private void running(final LunarTrack track) {
		reportStatus(LunarPluginStateReport.running(developerID, track));
	}

	private void stopping(final LunarTrack track) {
		reportStatus(LunarPluginStateReport.stopping(developerID, track));
	}

	private void stopping(final LunarTrack track, final Throwable err) {
		reportStatus(LunarPluginStateReport.stopping(developerID, track, err));
	}
	
	private void stopped(final LunarTrack track) {
		reportStatus(LunarPluginStateReport.stopped(developerID, track));
	}

	private void stopped(final LunarTrack track, final Throwable err) {
		reportStatus(LunarPluginStateReport.stopped(developerID, track, err));
	}
	
	void startTrack(final LunarTrack inputTrack) {
		final Observable<byte[]>        result      = getResultStream(inputTrack);
		final LunarTrack                resultTrack = getResultTrackTemplate(inputTrack.sourceID);
		final Observable<LunarMQWriter> output      = lunar.getOutputTrackStream(developerID, resultTrack);

		starting(resultTrack);
		
		output.subscribe( //TODO: Thread
			new Action1<LunarMQWriter>(){
				@Override
				public void call(final LunarMQWriter writer) {
					running(resultTrack);
					
					final Subscription  sub = result
							.subscribeOn(Schedulers.newThread()) //TODO: quazar
							.subscribe(
									writer,
									new Action1<Throwable>() {
										@Override
										public void call(final Throwable err) {
											stopping(resultTrack, err);
										}
									},
									new Action0() {
										@Override
										public void call() {
											stopping(resultTrack);
										}					
									}			
							);
					tracks.put(inputTrack.sourceID, sub);
				}				
			}, 
			new Action1<Throwable>(){
				@Override
				public void call(Throwable err) {
					stopped(resultTrack, err);
				}
			},
			new Action0() {
				@Override
				public void call() {
					stopped(resultTrack);
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
