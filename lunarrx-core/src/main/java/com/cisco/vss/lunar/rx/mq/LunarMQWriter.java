package com.cisco.vss.lunar.rx.mq;
import java.io.IOException;

import static com.cisco.vss.lunar.rx.mq.LunarMQConversions.*;

public class LunarMQWriter extends Converter<byte[], byte[]>{
	final private LunarMQSocket socket;
	
	LunarMQWriter(final LunarMQSocket socket) {
		super("LunarMQWriter");
		this.socket = socket;
	}
	
	public void close() throws IOException {
		socket.close();
	}

	@Override
	protected byte[] convert(byte[] buffer) throws Throwable {
		socket.write(buffer);
		return buffer;
	}

}
