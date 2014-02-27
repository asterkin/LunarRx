package com.cisco.vss.lunar.rx.mq;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;

public class LunarMQOnSubscribe implements Observable.OnSubscribe<byte[]> {
	private final LunarMQSocket socket;
	
	public LunarMQOnSubscribe(final LunarMQSocket socket) {
		this.socket = socket;
	}
	
	@Override
	public void call(final Subscriber<? super byte[]> subscriber) {
		while(!subscriber.isUnsubscribed()) {
			try {
				subscriber.onNext(socket.read());
			} catch (LunarMQException e) {
				if (e.getCode() == LunarMQException.StreamingError.LMQ_EOF ||
						e.getCode() == LunarMQException.StreamingError.LMQ_EOS) {
					subscriber.onCompleted();
					break;
				}
				if (e.isRecoverable() == false) //TODO: log recoverable error
				{
					subscriber.onError(e);
					break;
				}
			} catch (IOException e) {
				subscriber.onError(e);
				break;
			}
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
