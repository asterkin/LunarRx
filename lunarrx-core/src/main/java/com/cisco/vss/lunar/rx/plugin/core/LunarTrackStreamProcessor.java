package com.cisco.vss.lunar.rx.plugin.core;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

public class LunarTrackStreamProcessor implements Action1<LunarMQWriter> {
	private final static Logger      LOGGER = LogManager.getLogger();
	private final Lunar              lunar;
	private final LunarTrack         resultTrack;
	private final Observable<byte[]> result;

	public LunarTrackStreamProcessor(final Lunar lunar, final LunarTrack resultTrack, final Observable<byte[]> result) {
		this.lunar       = lunar;
		this.resultTrack = resultTrack;
		this.result      = result;
	}
	
	@Override
	public void call(final LunarMQWriter writer) {
		lunar.running(resultTrack);
		
		result.subscribe(
			writer,
			new Action1<Throwable>() {
				@Override
				public void call(final Throwable err) {
					lunar.stopped(resultTrack, err);
				}
			},
			new Action0() {
				@Override
				public void call() {
					lunar.stopped(resultTrack);
					try {
						writer.close();
					} catch (final IOException e) {
						LOGGER.error("Got an error while closing MQ socket for {}", resultTrack, e.fillInStackTrace());
					}
				}					
			}			
		);
	}
}
