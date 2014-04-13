package com.cisco.vss.lunar.rx.plugin.core.testplugin;

import java.util.Date;

import com.cisco.vss.lunar.rx.plugin.core.TrackItem;

public class TestTrack extends TrackItem {

	public TestTrack(int sourceID, Date time, String pluginName,
			String trackName, int trackVersion) {
		super(sourceID, time, pluginName, trackName, trackVersion);
	}

}
