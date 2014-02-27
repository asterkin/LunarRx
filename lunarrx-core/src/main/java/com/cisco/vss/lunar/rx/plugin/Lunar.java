package com.cisco.vss.lunar.rx.plugin;

import rx.Observable;

public class Lunar {
	private final String hostName;
	private final int    port;
	private final String developerID;
	
	public Lunar(final String hostName, final int port, final String developerID) {
		this.hostName    = hostName;
		this.port        = port;
		this.developerID = developerID;
	}
	
	public Observable<byte[]> getInputTrackStream(final String sourceID, final String pluginName, final String trackName) {
		return null;
	}
	
	public <T extends TrackItem> Observable<T> getInputTrackItemStream(final Class<T> clazz, final String sourceID, final String pluginName, final String trackName) {
		return null;
	}
	
	public LunarMQWriter getOutputTrackStream(final String sourceID, final String pluginName, final String trackName) {
		return null;
	}
	
	public <T extends TrackItem> LunarTractItemWriter<T> getOutputTractItemStream(final Class<T> clazz, final String sourceID, final String pluginName, final String trackName) {
		return null;
	}
	
}
