package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.lunar.rx.plugin.core.LunarConversions.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public abstract class LunarByteStreamTransformer {
	protected final Lunar                      lunar;
	protected final LunarPluginStateReporter   reporter;
	protected final String                     developerID;
	protected final Map<Integer, Subscription> tracks;
	protected final Logger                     logger;

	protected  LunarByteStreamTransformer(final Lunar lunar, final String developerID) {
		this.lunar       = lunar;
		this.reporter    = new LunarPluginStateReporter(lunar, developerID);
		this.developerID = developerID;
		this.tracks      = new HashMap<Integer, Subscription>();
		this.logger      = LogManager.getLogger();
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
							.subscribe(
									writer,
									new Action1<Throwable>() {
										@Override
										public void call(final Throwable err) {
											reporter.stopping(resultTrack, err);
										}
									},
									new Action0() {
										@Override
										public void call() {
											reporter.stopping(resultTrack);
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
			},
			new Action0() {
				@Override
				public void call() {
					reporter.stopped(resultTrack);
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
