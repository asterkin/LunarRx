package com.cisco.vss.lunar.rx.mq;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MessageHeader {
	private final int bodyLength;
	private final int sequenceNumber;
	
	public MessageHeader(final int bodyLength, final int sequenceNumber) {
		this.bodyLength     = bodyLength;
		this.sequenceNumber = sequenceNumber;
	}

	public static MessageHeader read(final BufferedInputStream stream) throws IOException, LunarMQException {
		final int bodyLength     = readInt(stream, (int)' ');
		final int sequenceNumber = readInt(stream, (int)'\n');
		
		if(0 == bodyLength)
			throw new LunarEndOfStreamException();
		return new MessageHeader(bodyLength, sequenceNumber);
	}
	
	private static int readInt(BufferedInputStream stream, int delim) throws IOException, LunarMQException {
		int value = 0;
		int b     = -1;
		
		while(delim != (b = stream.read())) {
			validate(b);
			value = value*10 + (b - (int)'0');
		}
		return value;
	}

	private static void validate(int code) throws LunarMQException {
		if(-1 == code)
			throw new LunarEndOfStreamException();
		if(!Character.isDigit(code))
			throw new LunarInvalidInputHeaderFormatException(code);
	}

	public void write(final OutputStream stream) throws IOException {
		stream.write(String.format("%d %d\n", bodyLength, sequenceNumber).getBytes());
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
		final byte[] buf         = new byte[bodyLength];
		int          bytesToRead = bodyLength;
		int          offset      = 0;
		
		while(bytesToRead > 0) {
			final int actual = stream.read(buf, offset, bytesToRead);
			if(-1 == actual)
				throw new LunarPrematureEndOfStreamException(sequenceNumber, bodyLength, offset);
			bytesToRead -= actual;
			offset      += actual;
		}
		return buf;
	}
}
