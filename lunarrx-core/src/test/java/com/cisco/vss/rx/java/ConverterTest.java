package com.cisco.vss.rx.java;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;

import org.junit.Test;

import com.google.gson.Gson;

import rx.functions.Action1;
import static com.cisco.vss.rx.java.Conversions.*;

public class ConverterTest {
    @Test
    public void testConverterOK() {
        final Converter<String, Integer> converter = new Converter<String, Integer>() {
        	@Override
			protected Integer convert(String message) throws Throwable {
        		return message.length();
            }
        };
        converter.call("Test").subscribe(
        	new Action1<Integer>(){
        		@Override
        		public void call(Integer t1) {
        			assertTrue(t1.equals(4));
        		}
        	}
        );
    }

    @Test
    public void testConverterException() {
    	final String MESSAGE                 = "Error ABC";
    	final ObjectHolder<Throwable> result = new ObjectHolder<Throwable>();
    	
        final Converter<String, String> converter = new Converter<String, String>() {
        	@Override
			protected String convert(String message) throws Throwable {
        		throw new Exception(MESSAGE);
            }
        };
        converter.call("Test").subscribe(
            new Action1<String>(){
                @Override
                public void call(String t1) {
                    fail("Should not get there");
                }
            },
            new Action1<Throwable>() {
                @Override
                public void call(Throwable t1) {
                	result.value = t1;
                }
            }
        );
        assertEquals(MESSAGE, result.value.getMessage());
    }
    
    @Test
    public void testByte2String() {
    	final String STR = "abcedfg";
    	final byte[] BUF = STR.getBytes();
    	final String RESULT = byte2String.call(BUF);
    	assertEquals(STR, RESULT);
    }
    
    private class TestData {
    	private Integer x;
    	private String  y;
    	public TestData(final Integer x, final String y) {
    		this.x = x;
    		this.y = y;
    	}
    	public Integer getX() {
    		return this.x;
    	}
    	public String getY() {
    		return this.y;
    	}
    }
    @Test
    public void testJsonString2Object() {
    	final TestData data   = new TestData(123,"abcdefg");
    	final Gson     gson   = new Gson();
    	final String   json   = gson.toJson(data);
    	jsonString2Object(TestData.class).call(json).subscribe(
    		new Action1<TestData>(){
    			@Override
    			public void call(TestData t1) {
    				assertEquals(new Integer(123), t1.getX());
    				assertEquals("abcdefg", t1.getY());
    			}	
    		}
    	);   
    }
    
    @Test
    public void testStringParser_OK() {
    	final String  IP      = "10.10.10.10";
    	final Integer PORT    = 129;
    	final String  MESSAGE = "Hallo";
    	final String  URL     = IP+":"+PORT+"/"+MESSAGE;
    	parseString("Lunar MQ URL", "([^:]+):([^/]+)/(.*)").call(URL).subscribe(
    		new Action1<Matcher>() {
				@Override
				public void call(Matcher matcher) {
					assertEquals(IP,              matcher.group(1));
					assertEquals(PORT.toString(), matcher.group(2));
					assertEquals(MESSAGE,         matcher.group(3));
				}
    		}
    	);
    }

    @Test
    public void testStringParser_ERROR() {
    	final String                  MESSAGE = "abcd";
    	final String                  PATTERN = "([^:]+):(.*)"; 
    	final String                  NAME    = "P1";
    	final ObjectHolder<Throwable> result  = new ObjectHolder<Throwable>();
    	final String                  ERROR   = String.format(
    			"String '%s' does not match the '%s' pattern",
    			MESSAGE,NAME);
    	parseString(NAME, PATTERN).call(MESSAGE).subscribe(
    		new Action1<Matcher>() {
                @Override
                public void call(Matcher macther) {
                    fail("Should not get there");
                }
            },
            new Action1<Throwable>() {
                @Override
                public void call(Throwable t1) {
                	result.value = t1;
                }
            }
    	);
    	assertEquals(ERROR, result.value.getMessage());
    }
    
    @Test
    public void testSynchHttpGet() throws IOException, InterruptedException {
    	final String         message   = "Hallo!!";
		final byte[][]       responses = new byte[][]{message.getBytes()};
		final HttpServerStub server    = new HttpServerStub(responses);
    	final URL            url       = new URL(
    			String.format("http://localhost:%d/hallo"
    			,server.startServer()));
    	
    	synchHttpGet.call(url).subscribe(
    		new Action1<String>() {
				@Override
				public void call(String t1) {
					assertEquals(message, t1);
				}    			
    		}
    	);
    	server.join();
    }
    
    @Test
    public void testMakeURL() {
    	final String urlArgs[] = {
    		"http",
    		"localhost",
    		"12345",
    		"tracks",
    		"sourceID",
    		"ABCDEFG",
    		"pluginName",
    		"myPlugin",
    		"trackName",
    		"TRACK-1"
    	};
    	makeURL.call(urlArgs).subscribe(
    		new Action1<URL>() {
				@Override
				public void call(URL url) {
					assertEquals("http",     url.getProtocol());
					assertEquals("localhost",url.getHost());
					assertEquals(12345,     url.getPort());
					assertEquals("/tracks?sourceID=ABCDEFG&pluginName=myPlugin&trackName=TRACK-1",url.getFile());
				}    			
    		}
    	);
    }
}
