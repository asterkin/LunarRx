package com.cisco.vss.lunar.rx.mq;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.junit.Test;
import com.cisco.vss.rx.java.ObjectHolder;
import rx.Observable;
import rx.functions.Action1;
import static com.cisco.vss.lunar.rx.mq.LunarMQConversions.*;
import static com.cisco.vss.lunar.rx.mq.LunarMQException.StreamingError.LMQ_OK;

public class LunarMQConversionsTest {

	@Test
	public void testUrlParser_OK() {
		parseMQUrl.call("localhost:1999/Hallo").subscribe(new Action1<Matcher>() {
			@Override
			public void call(Matcher matcher) {
				assertEquals("localhost", matcher.group(1));
				assertEquals("1999",      matcher.group(2));
				assertEquals("Hallo",     matcher.group(3));
			}
		});
	}
	
	@Test
	public void testUrlParser_ERROR() {
		final ObjectHolder<Throwable> error = new ObjectHolder<Throwable>();
		
		parseMQUrl.call("ABC").subscribe(
			new Action1<Matcher>() {
				@Override
				public void call(Matcher matcher) {
					fail("should not get there");
				}
			},
			new Action1<Throwable>() {
				@Override
				public void call(Throwable t1) {
					error.value = t1;
				}
			}
		);
		assertEquals("String 'ABC' does not match the 'MQ URL' pattern", error.value.getMessage());
	}
	
	@Test
	public void testMatcher2Socket() throws IOException, InterruptedException {
		final byte[] ACK = LMQ_OK.GetMessage().getBytes(); 
		final byte[][] responses = new byte[][]{
			ACK,
			"ABCDEFG".getBytes(),
			"1234567".getBytes()
		};
		final List<byte[]>      received = new ArrayList<byte[]>();
		final LunarMQServerStub server   = new LunarMQServerStub(responses);
		final Integer           port     = server.startServer();

		received.add(ACK);
		Observable.from("localhost:"+port.toString()+"/Hallo")
			.flatMap(parseMQUrl)
			.flatMap(connectToServer)
			.subscribe(
				new Action1<LunarMQSocket>() {
					@Override
					public void call(LunarMQSocket socket) {
						while(true)
							try {
								received.add(socket.read());
					        } catch (LunarMQException e) {
						      if (e.getCode() == LunarMQException.StreamingError.LMQ_EOF ||
								e.getCode() == LunarMQException.StreamingError.LMQ_EOS) 
						    	  break;
						      else
						    	  fail(e.getMessage());
						    } catch (IOException e) {
							  fail(e.getMessage());
						    }
					}					
				}		
			);
		server.join();
		assertArrayEquals(responses, received.toArray(new byte[0][]));
	}
	
	@Test
	public void testReadStream() throws IOException, InterruptedException {
		final byte[] ACK = LMQ_OK.GetMessage().getBytes(); 
		final byte[][] responses = new byte[][]{
			ACK,
			"ABCDEFG".getBytes(),
			"1234567".getBytes()
		};
		final List<byte[]> received     = new ArrayList<byte[]>();
		final LunarMQServerStub  server = new LunarMQServerStub(responses);
		final Integer            port   = server.startServer();
		final Observable<byte[]> obs    = Observable.from("localhost:"+port.toString()+"/Hallo")
		          .flatMap(parseMQUrl)
		          .flatMap(connectToServer)
		          .flatMap(readStream);
		
		received.add(ACK);
		obs.subscribe(
		    new Action1<byte[]>() {
		    	@Override
		    	public void call(byte[] message) {
		    		received.add(message);
		    	}
		    }		
        );
		server.join();
		assertArrayEquals(responses, received.toArray(new byte[0][]));
	}
}

