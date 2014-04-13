package com.cisco.vss.lunar.rx.plugin.core.testplugin;

import java.util.Date;

import com.cisco.vss.lunar.rx.plugin.core.LunarTrackItem;

public class TestTrack extends LunarTrackItem {

	public TestTrack(int sourceID, Date time, String pluginName,
			String trackName, int trackVersion) {
		super(sourceID, time, pluginName, trackName, trackVersion);
	}

}
