package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.lunar.rx.mq.LunarMQConversions.*;
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
/*
 * {"status":"up","list":[{"sourceID":"1","pluginName":"capsrx","protocols":[{"protocol":"LunarMQ","url":"54.195.242.59:7021/capsrx:caps:1"}],"trackName":"caps","mimeType":"json"}],"messageType":"tracks"}
 *
 */	
	//TODO: better encapsulation?
	public LunarTrack(final String pluginName, final String trackName) {
		this.pluginName = pluginName;
		this.trackName  = trackName;
	}

	public LunarTrack(final Integer sourceID, final String pluginName, final String trackName) {
		this.sourceID   = sourceID;
		this.pluginName = pluginName;
		this.trackName  = trackName;
		//TODO: defaults vs. specific classes
		this.protocol   = "LunarMQ";
		this.mime       = "json";
		this.deployed   = false;   
	}
	
	public Observable<byte[]> getBytestream() {
		return getMQStream(Observable.from(url));
	}
	
	public <T extends LunarTrackItem> Observable<T> getItems(Class<T> clazz) {
		return getMQStream(Observable.from(url), clazz);
	}
	
	@Override
	public String toString() {
		return object2JsonString(LunarTrack.class).call(this);
	}
	
	public String httpGetRequestPath() {
		return String.format("/tracks?sourceID=%s&pluginName=%s&trackName=%s",sourceID,pluginName,trackName);
	}

	private final static String streamerTemplate = "/streamer?"
            +"sourceID=%d"
            +"&pluginName=%s"
            +"&trackName=%s"
            +"&mimeType=%s"
            +"&enablePostToCore=%b"
            +"&protocol=%s" 
    		+"&developerID=%s";
	
	public String streamerRequestPath(final String developerID) {
		return String.format(streamerTemplate,sourceID,pluginName,trackName,mime,deployed,protocol,developerID);
	}

	@Override
	public String getId() {
		return String.format("%d/%s/%s", sourceID, pluginName, trackName);
	}

	public LunarTrack attachToSource(final Integer sourceID) {
		return new LunarTrack(sourceID, this.pluginName, this.trackName);
	}
}

