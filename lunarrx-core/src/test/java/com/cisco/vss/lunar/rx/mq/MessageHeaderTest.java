package com.cisco.vss.lunar.rx.mq;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

public class MessageHeaderTest {

	@Test
	public void testRead_Normal() throws IOException, LunarMQException {
		final byte[]              buf        = "128 10\nABC".getBytes();
		final InputStream         in         = new ByteArrayInputStream(buf);
		final BufferedInputStream stream     = new BufferedInputStream(in);
		final MessageHeader       header     = MessageHeader.read(stream);
		final byte[]              tail       = new byte[4];
		
		assertEquals(10,  header.getSequenceNumber());
		assertEquals(128, header.getBodyLength());
		assertEquals(3,   stream.read(tail));
		assertArrayEquals(new byte[]{'A','B', 'C', 0}, tail);
	}

	@Test(expected=LunarEndOfStreamException.class)
	public void testRead_EOS() throws IOException, LunarMQException {
		final byte[]              buf        = "".getBytes();
		final InputStream         in         = new ByteArrayInputStream(buf);
		final BufferedInputStream stream     = new BufferedInputStream(in);
		
		MessageHeader.read(stream);
	}
	
	@Test(expected=LunarEndOfStreamException.class)
	public void testRead_ZeroLength() throws IOException, LunarMQException {
		final byte[]              buf        = "0 10\n".getBytes();
		final InputStream         in         = new ByteArrayInputStream(buf);
		final BufferedInputStream stream     = new BufferedInputStream(in);
		
		MessageHeader.read(stream);
	}
	
	@Test(expected=LunarEndOfStreamException.class)
	public void testRead_EndOfStreamWhileReadingHeader() throws IOException, LunarMQException {
		final byte[]              buf        = "12 ".getBytes();
		final InputStream         in         = new ByteArrayInputStream(buf);
		final BufferedInputStream stream     = new BufferedInputStream(in);
		
		MessageHeader.read(stream);
	}

	@Test(expected=LunarInvalidInputHeaderFormatException.class)
	public void testRead_InvalidFormat() throws IOException, LunarMQException {
		final byte[]              buf        = "12:123".getBytes();
		final InputStream         in         = new ByteArrayInputStream(buf);
		final BufferedInputStream stream     = new BufferedInputStream(in);
		
		MessageHeader.read(stream);
	}
	
	@Test
	public void testCheckSequence_OK() throws LunarMessagesLostException {
		final MessageHeader header      = new MessageHeader(1024, 123);
		final int           newSequence = header.checkSequence(122);
		
		assertEquals(123, newSequence);
	}
	
	@Test
	public void testCheckSequence_MessageLost() {
		final MessageHeader header      = new MessageHeader(1024, 123);
		try {
			header.checkSequence(120);
			fail("Should not get there");
		} catch (LunarMessagesLostException ex) {
			assertEquals("2 LunarMQ messages were lost - are you working too slowly?", ex.getMessage());
		}
	}
	
	@Test
	public void testReadBody_OK() throws IOException, LunarPrematureEndOfStreamException {
		final MessageHeader       header = new MessageHeader(5, 123);
		final byte[]              buf    = "ABCDEF".getBytes();
		final InputStream         in     = new ByteArrayInputStream(buf);
		final BufferedInputStream stream = new BufferedInputStream(in);
		final byte[]              result = header.readBody(stream);
		
		assertArrayEquals("ABCDE".getBytes(), result);
		assertEquals(1, stream.available());
	}

	@Test(expected=LunarPrematureEndOfStreamException.class)
	public void testReadBody_PrematureEndOfStreamException() throws IOException, LunarPrematureEndOfStreamException {
		final MessageHeader       header = new MessageHeader(123, 15);
		final byte[]              buf    = "ABCDEF".getBytes();
		final InputStream         in     = new ByteArrayInputStream(buf);
		final BufferedInputStream stream = new BufferedInputStream(in);
		
		header.readBody(stream);
	}
	
	@Test
	public void testWrite() throws IOException {
		final MessageHeader         header = new MessageHeader(19, 127);
		final ByteArrayOutputStream out    = new ByteArrayOutputStream(1024);
		final BufferedOutputStream  stream = new BufferedOutputStream(out);
		
		header.write(stream);
		stream.write(new byte[]{'A', 'B', 'C'});
		stream.flush();
		assertEquals("19 127\nABC", out.toString());
	}
}
