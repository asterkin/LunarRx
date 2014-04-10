package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.rx.java.Conversions.jsonString2Object;
import static org.junit.Assert.*;
import org.junit.Test;
import rx.functions.Action1;
import com.cisco.vss.lunar.rx.plugin.core.TrackItem;

public class TrackItemTest {

	private class TestTrackItem extends TrackItem {
	}
	
	@Test
	public void testFromJson() {
		final String input =
			 	   "{"
						+"\"time\": 1393515036581,"
			 			+"\"source\":1,"
						+"\"vendor\":\"plugin1\","
			 			+"\"track\":\"track1\","
			 			+"\"version\":1"
					+"}"
		;
    	jsonString2Object(TestTrackItem.class).call(input).subscribe(
        		new Action1<TestTrackItem>(){
        			@Override
        			public void call(TestTrackItem item) {
        				assertEquals(1,         item.getSourceID());
        				assertEquals("plugin1", item.getPluginName());
        				assertEquals("track1",  item.getTrackName());
        				assertEquals(1,         item.getTrackVersion());
        			}	
        		}
        	);   
	}
}
