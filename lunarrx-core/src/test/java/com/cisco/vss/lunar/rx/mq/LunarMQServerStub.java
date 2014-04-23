package com.cisco.vss.lunar.rx.mq;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;

public class LunarMQServerStub extends TcpServerStub {
	protected int outMsgSeq;
	public LunarMQServerStub(byte[][] responses, boolean isReceiver) throws IOException {
		super(responses, isReceiver);
		this.outMsgSeq = 0;
	}
	
	public LunarMQServerStub(byte[][] responses) throws IOException {
		this(responses, false);
	}
	
	@Override
	protected int readRequestHeader(final BufferedReader reader) throws IOException {
		final String header = reader.readLine();
	
		if(null == header) throw new EOFException();
		logRequest(header);
		return Integer.parseInt(header.split(" ")[0]);
	}
	@Override
	protected String formatResponseHeader(byte[] response) {
		return String.format("%d %d\n", response.length,  outMsgSeq++);
	}
}
