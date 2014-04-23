package com.cisco.vss.lunar.rx.plugin.core;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func2;

public class LunarByteStreamTransformer {
	protected final static Logger                 LOGGER = LogManager.getLogger();
	protected final Lunar                         lunar;
	private   final LunarTrack                    sourceTemplate;
	private   final LunarTrack                    resultTemplate;
	private   final LunarTrackProcessorThreadPool threadPool;
	private   final Map<Integer, Subscription>    tracks;

	protected  LunarByteStreamTransformer(final Lunar lunar, final LunarTrack sourceTemplate, final Func2<Observable<byte[]>, LunarTrack, Observable<? extends byte[]>> transform, final LunarTrack resultTemplate) {
		this.lunar          = lunar;
		this.threadPool     = new LunarTrackProcessorThreadPool(lunar, transform); 
		this.tracks         = new HashMap<Integer, Subscription>();
		this.sourceTemplate = sourceTemplate;
		this.resultTemplate = resultTemplate;
	}
	
	public void run() {
		//TODO: re-start
		lunar.getTracks(this.sourceTemplate)
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
						LOGGER.fatal("Got an error while processing Track status", err);
					}
	
				},
				new Action0() {
					@Override
					public void call() {
						LOGGER.warn("Unexpected end of Tracks status update stream. Is Lunar up?");
					}					
				}			
		);
	}

	public int getNumberOfActiveTracks() {
		synchronized(tracks) { return tracks.size(); }
	}
	
	private void reflectTrackStatus(final LunarNotify<LunarTrack> notify) {
		final LunarTrack sourceTrack = notify.getItem();
		final LunarTrack resultTrack = resultTemplate.attachToSource(sourceTrack.sourceID);
		if(notify instanceof LunarAddTrack<?>) { 
			final Subscription subs = threadPool.startTrack(sourceTrack, resultTrack, 
				new Action0() {
					@Override
					public void call() {
						tracks.remove(resultTrack.sourceID);
					}				
				}
			);
			tracks.put(resultTrack.sourceID, subs);
		} else if (notify instanceof LunarRemoveTrack<?>) {
			final Subscription subs = tracks.get(resultTrack.sourceID);
			if(null != subs) {
				lunar.stopping(resultTrack);
				//TODO: need to fix the subscription structure to make it possibe. Hopefully not top priority
//				subs.unsubscribe();
				LOGGER.debug("Stopping Track {}", resultTrack);
			}
		}
	}
}
