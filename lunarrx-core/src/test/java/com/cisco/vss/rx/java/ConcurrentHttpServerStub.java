package com.cisco.vss.rx.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

//TODO: duplication!
public class ConcurrentHttpServerStub extends Thread {
	private final ServerSocket        server;
	private final Map<String,String>  responses;
	private final Map<String,String>  requests;
	
	public ConcurrentHttpServerStub(final Map<String, String> responses) throws IOException {
		this.responses = responses;
		this.requests  = new HashMap<String,String>();
		this.server    = new ServerSocket(0);
		this.server.setSoTimeout(50); //Should be enough for tests
	}

	public int startServer() {
		super.start();
		return server.getLocalPort();
	}
	
	@Override
	public void run() {
		while((responses.size() > 0) && !this.isInterrupted())
			try {
				final Socket         client = server.accept();
			    final BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			    final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			    
			    final String uri = receive(reader);
			    sendResponse(uri, writer);
				reader.close();
				writer.close();
				client.close();
			} catch(SocketTimeoutException e) {
				
			}catch (IOException e) {
				e.printStackTrace();
			}
	}

	public Map<String, String> getRequests() {
		return this.requests;
	}
	
	private void sendResponse(final String uri, final BufferedWriter writer) throws IOException {
		final String response = responses.get(uri);
		send(writer, response);
		writer.flush();
		responses.remove(uri);
	}

	private String receive(final BufferedReader reader) throws IOException {
		final String request[] = reader.readLine().split(" ");
		final String method    = request[0]; 
		final String uri       = request[1];
		
		if(hasBody(method)) readBody(uri, reader);
		return uri;
	}

	private void readBody(final String uri, final BufferedReader reader) throws IOException {
		String line;
		int    length = 0;
		while(!"".equals(line = reader.readLine()))
			if(line.startsWith("Content-Length"))
				length = Integer.parseInt(line.split(" ")[1]);
		char[] buf = new char[length];
		reader.read(buf);
		this.requests.put(uri, new String(buf));
	}

	private boolean hasBody(final String method) {
		if("POST".equals(method)) return true;
		if("PUT".equals(method))  return true; 
		return false;
	}

	private void send(final BufferedWriter writer, final String response) throws IOException {
		if(null == response)
			writer.write(formatNotFoundResponse());
		else {
			writer.write(formatResponseHeader(response));
			writer.write(response);
		}
	}

	private String formatNotFoundResponse() {
		return "HTTP/1.x 404 Not Found\n"
				+"Connection: close\n"
				+"\n";
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
