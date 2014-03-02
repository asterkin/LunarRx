package com.cisco.vss.lunar.rx.mq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.cisco.vss.lunar.rx.mq.LunarMQException.StreamingError.*;

public class LunarMQSocketTest {
	@Test
	public void testReceive_OK() throws IOException, LunarMQException, InterruptedException {
		final byte[][] responses = new byte[][]{
			LMQ_OK.GetMessage().getBytes(),
			"ABCEFG".getBytes(),
			"12345".getBytes()
		};
		final LunarMQServerStub server   = new LunarMQServerStub(responses);
		final int               port     = server.startServer();
		final LunarMQSocket     client   = LunarMQSocket.createSocket("localhost", port, "Hallo");
		final byte[][]          received = readResponses(client, responses[0]);
		client.close();
		server.join();
		assertArrayEquals(responses, received);
	}

	private byte[][] readResponses(final LunarMQSocket client, final byte[] first) throws IOException {
		final List<byte[]> received = new ArrayList<byte[]>();
		received.add(first);
		while(true)
			try {
				received.add(client.read());
			} catch (LunarMQException e) {
				break;
			}
		return received.toArray(new byte[0][]);
	}
	
	@Test
	public void testSend_OK() throws IOException, LunarMQException, InterruptedException {
		final byte[][] responses = new byte[][]{
				LMQ_OK.GetMessage().getBytes()
		};
		final byte[][] requests = new byte[][]{
				"ABCEFG".getBytes(),
				"ZYXWVT".getBytes(),
				"12345" .getBytes()
			};
		final LunarMQServerStub server = new LunarMQServerStub(responses, true);
		final int               port   = server.startServer();
		final LunarMQSocket     client = LunarMQSocket.createSocket("localhost", port, "Hallo");

		sendRequests(client, requests);
		client.close();
		server.join();
		final byte[][] expected = makeExpected("Hallo", requests);
		final byte[][] actual   = server.getRequests();
		assertArrayEquals(expected, actual);
	}

	private byte[][] makeExpected(final String handshake, byte[][] requests) {
		List<byte[]> expected = new ArrayList<byte[]>();
		addExpected(expected, handshake.getBytes());
		for(byte[] request : requests)
			addExpected(expected, request);
		return expected.toArray(new byte[0][]);
	}

	private void addExpected(List<byte[]> expected, byte[] buf) {
		expected.add(String.format("%d %d", buf.length, expected.size()/2).getBytes());
		expected.add(buf);
	}

	private void sendRequests(LunarMQSocket client, byte[][] requests) throws IOException, LunarMQException {
		for(byte[] request : requests)
			client.write(request);
	}
}
