package com.cisco.vss.lunar.rx.plugin.schema.capsrx;

import java.util.Date;

import com.cisco.vss.lunar.rx.plugin.core.TrackItem;

public class Caps extends TrackItem {
	public final String[] caps;
	
	public Caps(int sourceID, Date time, String pluginName, String trackName, int trackVersion, final String[] caps) {
		super(sourceID, time, pluginName, trackName, trackVersion);
		this.caps = caps; 
	}

}
