package com.cisco.vss.lunar.rx.plugin.core;
import java.io.IOException;

import rx.functions.Action1;

import com.cisco.vss.lunar.rx.mq.LunarMQException;
import com.cisco.vss.lunar.rx.mq.LunarMQSocket;

public class LunarMQWriter implements Action1<byte[]>{
	final private LunarMQSocket socket;
	
	public LunarMQWriter(final LunarMQSocket socket) {
		this.socket = socket;
	}
	
	@Override
	public void call(byte[] buffer) {
		try {
			socket.write(buffer);
		} catch (IOException | LunarMQException e) {
			// TODO Report Track Error
			e.printStackTrace();
		}
	}

	public void close() throws IOException {
		socket.close();
	}

}
