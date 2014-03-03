package com.cisco.vss.lunar.rx.plugin;

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.Date;

import org.junit.Test;

import com.cisco.vss.lunar.rx.plugin.core.TrackItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TrackItemTest {

	private class TestTrackItem extends TrackItem {
		public TestTrackItem(int sourceID, Date time, String pluginName,
				String trackName, int trackVersion) {
			super(sourceID, time, pluginName, trackName, trackVersion);
		}
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
		JsonSerializer<Date> ser = new JsonSerializer<Date>() {

			@Override
			public JsonElement serialize(Date src, Type typeOfSrc,
					JsonSerializationContext context) {
			    return src == null ? null : new JsonPrimitive(src.getTime());
			}
		};

		JsonDeserializer<Date> deser = new JsonDeserializer<Date>() {

			@Override
			public Date deserialize(JsonElement json, Type typeOfT,
					JsonDeserializationContext context)
					throws JsonParseException {
				return json == null ? null : new Date(json.getAsLong());			}
		};

			Gson gson = new GsonBuilder()
			   .registerTypeAdapter(Date.class, ser)
			   .registerTypeAdapter(Date.class, deser).create();
			
		final TestTrackItem item = gson.fromJson(input, TestTrackItem.class);
		
		assertEquals(1,         item.getSourceID());
		assertEquals("plugin1", item.getPluginName());
		assertEquals("track1",  item.getTrackName());
		assertEquals(1,         item.getTrackVersion());
		System.out.println(item.getTime());
	}

}
