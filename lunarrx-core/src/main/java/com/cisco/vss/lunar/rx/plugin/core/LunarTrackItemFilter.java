package com.cisco.vss.lunar.rx.plugin.core;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import static com.cisco.vss.rx.java.Conversions.*;

public class LunarTrackItemFilter<T extends TrackItem, R extends TrackItem> implements LunarPlugin<T, R> {
	private final Observable<LunarNotify<LunarTrack>> tracksStatus;
	private final Func1<T, Observable<R>>             transform;
	private final Observable<LunarMQWriter>           output;
	private final Map<Integer, Subscription>          tracks;
	
	public LunarTrackItemFilter(final Observable<LunarNotify<LunarTrack>> tracksStatus,
								final Func1<T, Observable<R>>             transform,
								final Observable<LunarMQWriter>           output) {
		this.tracksStatus = tracksStatus;
		this.transform    = transform;
		this.output       = output;
		this.tracks       = new HashMap<Integer, Subscription>();
	}

	@Override
	public void run() {
		//TODO: re-start
		tracksStatus.subscribe(
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

	private void startTrack(final LunarTrack track) {
		//TODO: obtain new writer per track
		final Observable<T> input  = track.getItems(null); //TODO: clazz for input
		final Observable<R> result = input.flatMap(transform);
		output.subscribe(
			new Action1<LunarMQWriter>(){
				@Override
				public void call(final LunarMQWriter writer) {
					final Subscription  sub = result
							.map(object2JsonString(null)) //TODO: clazz for result
							.map(string2Byte)
							.subscribeOn(Schedulers.newThread())
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
