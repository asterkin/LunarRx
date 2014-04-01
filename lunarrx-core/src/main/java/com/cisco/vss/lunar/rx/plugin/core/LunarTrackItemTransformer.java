package com.cisco.vss.lunar.rx.plugin.core;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import static com.cisco.vss.lunar.rx.plugin.core.LunarConversions.*;

public abstract class LunarTrackItemTransformer<T extends TrackItem, R extends TrackItem> {
	private final Lunar                      lunar;
	private final String                     developerID;
	private final Class<T>                   inputType;
	private final Class<R>                   resultType;
	private final Map<Integer, Subscription> tracks;
	
	protected LunarTrackItemTransformer(final Lunar lunar, final String developerID, final Class<T> inputType, final Class<R> resultType) {
		this.lunar       = lunar;
		this.developerID = developerID;
		this.inputType   = inputType;
		this.resultType  = resultType;
		this.tracks      = new HashMap<Integer, Subscription>();
	}
	
	public void run() throws MalformedURLException {
		//TODO: re-start
		lunar.getTracks()
		.filter(pluginTrack(getInputTrackTemplate()))
		.subscribe(
				new Action1<LunarNotify<LunarTrack>>() {
					@Override
					public void call(final LunarNotify<LunarTrack> notify) {
						try {
							reflectTrackStatus(notify);
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							//could not get there
							e.printStackTrace();
						}
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

	protected abstract LunarTrack              getInputTrackTemplate();
	protected abstract LunarTrack              getResultTrackTemplate(final Integer sourceID);
	protected abstract Func1<T, Observable<R>> transform();

	private void reflectTrackStatus(final LunarNotify<LunarTrack> notify) throws MalformedURLException {
		final LunarTrack track = notify.getItem();
		if(notify instanceof LunarAdd<?>) startTrack(track);
		else if (notify instanceof LunarRemove<?>) stopTrack(track);
	}

	private void stopTrack(final LunarTrack track) {
		final Integer id = track.sourceID;
		
		this.tracks.get(id).unsubscribe();
		this.tracks.remove(id);
	}

	private void startTrack(final LunarTrack track) throws MalformedURLException {
		final Observable<T>             input  = track.getItems(inputType); 
		final Observable<R>             result = input.flatMap(transform());
		final Observable<LunarMQWriter> output = lunar.getOutputTrackStream(developerID, getResultTrackTemplate(track.sourceID)); 
		output.subscribe( //TODO: Thread
			new Action1<LunarMQWriter>(){
				@Override
				public void call(final LunarMQWriter writer) {
					final Subscription  sub = result
							.map(object2JsonString(resultType))
							.map(string2Byte)
							.subscribeOn(Schedulers.newThread()) //TODO: quazar
							.subscribe(
									writer, //TODO: report status
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
					tracks.put(track.sourceID, sub);
				}				
			}, 
			new Action1<Throwable>(){
				@Override
				public void call(Throwable t1) {
					//TODO: report status
				}
			}
		);		
	}
}
