package com.cisco.vss.rx.java;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import org.junit.Test;

import rx.Observable;
import rx.functions.Action1;
import static com.cisco.vss.rx.java.Conversions.*;

public class ConverterTest {
    @Test
    public void testConverterOK() {
        final Converter<String, Integer> converter = new Converter<String, Integer>("testConverter") {
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
    	
        final Converter<String, String> converter = new Converter<String, String>("buggyConverter") {
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
    	private Date    d;
    	private Integer x;
    	private String  y;
    	public TestData(final Integer x, final String y) {
    		this.d = new Date();
    		this.x = x;
    		this.y = y;
    	}
    	public Integer getX() {
    		return this.x;
    	}
    	public String getY() {
    		return this.y;
    	}
    	public Date getD() {
    		return d;
    	}
    }
    @Test
    public void testJsonConversion() {
    	final TestData data   = new TestData(123,"abcdefg");
    	final String   json   = object2JsonString(TestData.class).call(data);
    	jsonString2Object(TestData.class).call(json).subscribe(
    		new Action1<TestData>(){
    			@Override
    			public void call(TestData t1) {
    				assertEquals(new Integer(123), t1.getX());
    				assertEquals("abcdefg", t1.getY());
    				assertTrue(new Date().after(t1.getD()));
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
    public void testFlatten() {
    	final String[] INPUT = new String[] {"AAA", "BBB", "CCC"};
    	final ObjectHolder<List<String>> result = new ObjectHolder<List<String>>(new ArrayList<String>());
    	Observable.from(new String[][]{INPUT})
    	.flatMap(flatten(String.class))
    	.subscribe(
    		new Action1<String>() {
				@Override
				public void call(final String t1) {
					result.value.add(t1);
				}
    		}
    	);
    	assertArrayEquals(INPUT, result.value.toArray());
    }
}
