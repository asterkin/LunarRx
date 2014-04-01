package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.lunar.rx.mq.LunarMQConversions.connectToServer;
import static com.cisco.vss.lunar.rx.mq.LunarMQConversions.parseMQUrl;
import static com.cisco.vss.lunar.rx.mq.LunarMQConversions.readStream;
import static com.cisco.vss.rx.java.Conversions.byte2String;
import static com.cisco.vss.rx.java.Conversions.jsonString2Object;
import rx.Observable;

import com.google.gson.annotations.SerializedName;

public class LunarTrack implements LunarEntity {
	public Integer sourceID;
	public String  protocol;
	@SerializedName("track")
	public String  trackName;
	public boolean deployed;
	public String  mime;
	public String  url;
	@SerializedName("plugin")
	public String  pluginName;
	
	public class Response            extends LunarDataResponse<LunarTrack[]> {}
	public class StatusUpdateMessage extends LunarStatusUpdateMessage<LunarTrack> {}
	
	//TODO: better encapsulation?
	public LunarTrack(final Integer sourceID, final String pluginName, final String trackName) {
		this.sourceID   = sourceID;
		this.pluginName = pluginName;
		this.trackName  = trackName;
		//TODO: defaults vs. specific classes
		this.protocol   = "LunarMQ";
		this.mime       = "json";
		this.deployed   = false;   
	}
	
	public Observable<byte[]> getBitestream() {
		return Observable.from(url)
				.flatMap(parseMQUrl)
				.flatMap(connectToServer)
				.flatMap(readStream);
	}
	
	public <T extends TrackItem> Observable<T> getItems(Class<T> clazz) {
		return getBitestream()
				.map(byte2String)
				.flatMap(jsonString2Object(clazz));
	}
	
	public String httpGetRequestPath() {
		return String.format("/tracks?sourceID=%s&pluginName=%s&trackName=%s",sourceID,pluginName,trackName);
	}

	private final static String streamerTemplate = "/streamer?"
            +"sourceID=%d"
            +"&pluginName=%s"
            +"&trackName=%s"
            +"&mime=%s"
            +"&enablePostToCore=%b"
            +"&protocol=%s" 
    		+"&developerID=%s";
	
	public String streamerRequestPath(final String developerID) {
		return String.format(streamerTemplate,sourceID,pluginName,trackName,mime,deployed,protocol,developerID);
	}

	@Override
	public Long getId() {
		int hash = 7;
		hash = 31*hash + pluginName.hashCode();
		hash = 31+hash + trackName.hashCode();
		long id = sourceID << 32 | hash; 
		return id;
	}
}

