package com.cisco.vss.rx.java;

import static org.junit.Assert.*;
import java.io.IOException;
import java.net.URL;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.Rule;
import rx.functions.Action1;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import static com.cisco.vss.rx.java.Conversions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class HttpConverterTest {
	private static final String               PATH    = "http://localhost:3000/hallo";
	private static final String               MESSAGE = "{message: \"Hallo\"}";
	private static final ObjectHolder<String> result  = new ObjectHolder<String>();
	private static final Action1<String>      onNext  = new  Action1<String>() {
		@Override
		public void call(final String message) {
			result.value = message;
		}    			
	};

	@ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(3000);
    @Rule
    public WireMockClassRule instanceRule = wireMockRule;
    
    @Test
    public void testSynchHttpGet() throws IOException, InterruptedException {
    	stubFor(get(urlEqualTo("/hallo"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(MESSAGE)));
    	
    	synchHttpGet.call(new URL(PATH)).subscribe(onNext);
		assertEquals(MESSAGE, result.value);
    }
    
    @Test
    public void testSynchHttpPost() throws IOException, InterruptedException {
    	stubFor(post(urlEqualTo("/hallo"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("OK")));
        
    	synchHttpPost(MESSAGE).call(new URL(PATH)).subscribe(onNext);
    	
        verify(postRequestedFor(urlMatching("/hallo"))
                .withRequestBody(equalTo(MESSAGE))
                .withHeader("Content-Type", equalTo("application/json")));
        
        assertEquals("OK", result.value);
    }
}
