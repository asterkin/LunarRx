package com.cisco.vss.lunar.rx.plugin.core;

import rx.functions.Func1;

public class LunarPluginTrackFilter implements Func1<LunarNotify<LunarTrack>, Boolean> {
	private final String pluginName;
	private final String trackName;
	
	public LunarPluginTrackFilter(final String pluginName, final String trackName) {
		this.pluginName = pluginName;
		this.trackName  = trackName;
	}
	
	@Override
	public Boolean call(final LunarNotify<LunarTrack> notify) {
		final LunarTrack track = notify.getItem();
		return    track.pluginName.equals(this.pluginName)
			   && track.trackName.equals(this.trackName);
	}

}
