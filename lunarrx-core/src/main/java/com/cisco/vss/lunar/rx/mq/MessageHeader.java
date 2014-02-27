package com.cisco.vss.lunar.rx.mq;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

public class MessageHeader {
	private final int sequenceNumber;
	private final int bodyLength;
	
	public MessageHeader(final int sequenceNumber, final int bodyLength) {
		this.sequenceNumber = sequenceNumber;
		this.bodyLength     = bodyLength;
	}

	public static MessageHeader read(final BufferedInputStream stream) throws IOException, LunarMQException {
		if(0 == stream.available())
			throw new LunarEndOfStreamException();
		final int sequenceNumber = readInt(stream, ' ');
		final int bodyLength     = readInt(stream, '\n');
		
		if(0 == bodyLength)
			throw new LunarEndOfStreamException();
		return new MessageHeader(sequenceNumber, bodyLength);
	}
	
	private static int readInt(BufferedInputStream stream, char delim) throws IOException, LunarMQException {
		int value = 0;
		int b     = -1;
		
		while(delim != (b  = stream.read())) {
			validate(b);
			value = value*10 + (b - '0');
		}
		return value;
	}

	private static void validate(int code) throws LunarMQException {
		if(-1 == code)
			throw new LunarPrematureEndOfStreamException();
		if(!Character.isDigit(code))
			throw new LunarInvalidInputHeaderFormatException(code);
	}

	public void write(final BufferedOutputStream stream) throws IOException {
		stream.write(String.format("%d %d\n", sequenceNumber, bodyLength).getBytes());
	}
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	
	public int getBodyLength() {
		return bodyLength;
	}

	public int checkSequence(int prevSeq) throws LunarMessagesLostException {
		if(sequenceNumber != (prevSeq +1))
			throw new LunarMessagesLostException(sequenceNumber - prevSeq -1);
		return sequenceNumber;
	}

	public byte[] readBody(final BufferedInputStream stream) throws IOException, LunarPrematureEndOfStreamException {
		final byte[] buf    = new byte[bodyLength];
		final int    actual = stream.read(buf);
		
		if(actual < bodyLength)
			throw new LunarPrematureEndOfStreamException();
		return buf;
	}
}
