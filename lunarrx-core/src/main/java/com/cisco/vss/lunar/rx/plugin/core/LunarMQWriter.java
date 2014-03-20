package com.cisco.vss.lunar.rx.plugin.core;
import java.io.IOException;

import com.cisco.vss.lunar.rx.mq.LunarMQException;
import com.cisco.vss.lunar.rx.mq.LunarMQSocket;

public class LunarMQWriter {
	final private LunarMQSocket socket;
	
	public LunarMQWriter(final LunarMQSocket socket) {
		this.socket = socket;
	}
	
	public void write(final byte[] buffer) throws IOException, LunarMQException {
		socket.write(buffer);
	}

}
