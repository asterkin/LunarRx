package com.cisco.vss.rx.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

//TODO: duplication!
public class ConcurrentHttpServerStub extends Thread {
	private final ServerSocket        server;
	private final Map<String,String>  responses;
	
	public ConcurrentHttpServerStub(final Map<String, String> responses) throws IOException {
		this.server    = new ServerSocket(0);
		this.responses = responses;		
	}

	public int startServer() {
		super.start();
		return server.getLocalPort();
	}
	
	@Override
	public void run() {
		while(responses.size() > 0)
			try {
				final Socket         client = server.accept();
			    final BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			    final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			    
			    final String uri = receive(reader);
			    sendResponses(uri, writer);
				reader.close();
				writer.close();
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private void sendResponses(final String uri, final BufferedWriter writer) throws IOException {
		final String response = responses.get(uri);
		send(writer, response);
		writer.flush();
		responses.remove(uri);
	}

	private String receive(final BufferedReader reader) throws IOException {
		final String uri = reader.readLine().split(" ")[1];
		
		while(!"".equals((reader.readLine())));
		return uri;
	}

	private void send(final BufferedWriter writer, final String response) throws IOException {
		writer.write(formatResponseHeader(response));
		writer.write(response);
	}

	private String formatResponseHeader(String response) {
		final String header = 
			"HTTP/1.x 200 OK\n"
			+"Connection: close\n"
			+String.format("Content-Length: %d\n", response.length())
			+"\n";	
		return header;
	}
}
