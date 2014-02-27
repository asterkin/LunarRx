package com.cisco.vss.lunar.rx.plugin;

import com.google.gson.annotations.SerializedName;

public class TrackInfo {
	public String  sourceID;
	public String  protocol;
	@SerializedName("track")
	public String  trackName;
	public boolean deployed;
	public String  mime;
	public String  url;
	@SerializedName("plugin")
	public String  pluginName;
	
	public TrackInfo(final String sourceID, final String pluginName, final String trackName) {
		this.sourceID   = sourceID;
		this.pluginName = pluginName;
		this.trackName  = trackName;
	}
	
	public String[] httpGetRequestArguments(final String IP, final Integer port) {
		return new String[] {
			"http",
			IP,
			port.toString(),
			"/tracks",
			"sourceID",	 this.sourceID,
			"pluginName",this.pluginName,
			"trackName", this.trackName
		};
	}
}

