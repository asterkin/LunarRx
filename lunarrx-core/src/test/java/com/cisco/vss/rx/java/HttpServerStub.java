package com.cisco.vss.rx.java;

import java.io.BufferedReader;
import java.io.IOException;

public class HttpServerStub extends TcpServerStub {

	public HttpServerStub(byte[][] responses) throws IOException {
		super(responses, false);
	}

	@Override
	protected int readRequestHeader(BufferedReader reader) throws IOException {
		String line;
		
		while(!"".equals((line = reader.readLine())))
			logRequest(line);
		return 0;
	}

	@Override
	protected String formatResponseHeader(byte[] response) {
		final String header = 
			"HTTP/1.x 200 OK\n"
			+"Connection: close\n"
			+String.format("Content-Length: %d\n", response.length)
			+"\n";	
		return header;
	}
}
