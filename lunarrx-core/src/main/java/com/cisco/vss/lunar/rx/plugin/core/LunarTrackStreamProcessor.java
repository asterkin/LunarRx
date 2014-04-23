package com.cisco.vss.lunar.rx.plugin.core;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cisco.vss.lunar.rx.mq.LunarMQWriter;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

class LunarTrackStreamProcessor implements Action1<LunarMQWriter> {
	private final static Logger                LOGGER = LogManager.getLogger();
	private final Lunar                        lunar;
	private final LunarTrack                   resultTrack;
	private final Observable<? extends byte[]> result;

	LunarTrackStreamProcessor(final Lunar lunar, final LunarTrack resultTrack, final Observable<? extends byte[]> result) {
		this.lunar       = lunar;
		this.resultTrack = resultTrack;
		this.result      = result;
	}
	
	@Override
	public void call(final LunarMQWriter writer) {
		lunar.running(resultTrack);
		
		result
		.flatMap(writer)
		.subscribe(
			new Action1<byte[]>() {
				@Override
				public void call(final byte[] buffer) {
					LOGGER.debug("Sent {} to {}", buffer, resultTrack);
				}
			},
			new Action1<Throwable>() {
				@Override
				public void call(final Throwable err) {
					lunar.stopped(resultTrack, err);
					LOGGER.error("Got an error while generating {}", resultTrack, err.fillInStackTrace());
					closeSocket(writer);
				}
			},
			new Action0() {
				@Override
				public void call() {
					lunar.stopped(resultTrack);
					closeSocket(writer);
				}					
			}						
		);
	}
	
	private void closeSocket(final LunarMQWriter writer) {
		try {
			writer.close();
		} catch (final IOException err) {
			LOGGER.error("Got an error while closing MQ socket for {}", resultTrack, err.fillInStackTrace());
		}		
	}
}
