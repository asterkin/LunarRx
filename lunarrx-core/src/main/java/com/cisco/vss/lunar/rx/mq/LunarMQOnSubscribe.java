package com.cisco.vss.lunar.rx.mq;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Observable;
import rx.Subscriber;

public class LunarMQOnSubscribe implements Observable.OnSubscribe<byte[]> {
	private final static Logger LOGGER = LogManager.getLogger();
	private final LunarMQSocket socket;
	
	public LunarMQOnSubscribe(final LunarMQSocket socket) {
		this.socket = socket;
	}
	
	@Override
	public void call(final Subscriber<? super byte[]> subscriber) {
		while(!subscriber.isUnsubscribed()) {
			try {
				final byte[] buf = socket.read();
				LOGGER.trace("Got new message from Lunar MQ Stream {}", buf);
				subscriber.onNext(buf);
			} catch (LunarMQException e) {
				LOGGER.debug("Got Lunar MQ Exception {}", e.toString());
				if (e.getCode() == LunarMQException.StreamingError.LMQ_EOF ||
						e.getCode() == LunarMQException.StreamingError.LMQ_EOS) {
					subscriber.onCompleted();
					LOGGER.trace("End of Lunar MQ Stream");
					break;
				}
				if (e.isRecoverable() == false) 
				{
					LOGGER.error("Unrecoverable Lunar MQ Exception {}", e.toString());
					subscriber.onError(e);
					break;
				}
			} catch (IOException e) {
				LOGGER.error("Unrecoverable IO Exception {}", e.toString());
				subscriber.onError(e);
				break;
			}
		}
		try {
			socket.close();
		} catch (IOException e) {
			LOGGER.debug("IO Exception while closing Lunar MQ Socket", e.toString());
		}
	}
}
