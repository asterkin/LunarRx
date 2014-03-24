package com.cisco.vss.lunar.rx.plugin.core;

import com.google.gson.annotations.SerializedName;

public class LunarTrack {
	public Integer sourceID;
	public String  protocol;
	@SerializedName("track")
	public String  trackName;
	public boolean deployed;
	public String  mime;
	public String  url;
	@SerializedName("plugin")
	public String  pluginName;
	
	public class Response extends LunarResponse<LunarTrack[]> {}
	
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
}

