package com.cisco.vss.lunar.rx.mq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class TcpServerStub extends Thread {
	private final ServerSocket server;
	private final byte[][]     responses;
	private final List<byte[]> requests;
	private final boolean      isReceiver;
	
	public TcpServerStub(final byte[][] responses, boolean isReceiver) throws IOException {
		this.server    = new ServerSocket(0);
		this.responses = responses;
		this.requests  = new ArrayList<byte[]>();
		this.isReceiver= isReceiver;
	}

	public int startServer() {
		super.start();
		return server.getLocalPort();
	}
	
	public byte[][] getRequests() {
		return requests.toArray(new byte[0][]);
	}
	
	@Override
	public void run() {
		try {
			final Socket         client = server.accept();
		    final BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		    final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
		    
		    receive(reader);
		    sendResponses(writer);	    
			if(isReceiver)
				receiveRequests(reader);
			reader.close();
			writer.close();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void receiveRequests(final BufferedReader reader)	throws IOException {
		while(receive(reader));
	}

	private void sendResponses(final BufferedWriter writer) throws IOException {
		for(byte [] response : responses) {
			send(writer, response);
			writer.flush();
		}
	}

	private boolean receive(final BufferedReader reader) throws IOException {
		try {
			final int length = readRequestHeader(reader);
			final char[] buf = new char[length];
	
			reader.read(buf);
			logRequest(new String(buf));
			return true;
		} catch (EOFException e) {
		}
		return false;
	}

	protected void logRequest(final String header) {
		requests.add(header.getBytes());
	}

	private void send(final BufferedWriter writer, byte[] response) throws IOException {
		writer.write(formatResponseHeader(response));
		writer.write(new String(response));
	}

	protected abstract int    readRequestHeader(final BufferedReader reader) throws IOException;	
	protected abstract String formatResponseHeader(byte[] response);
}
