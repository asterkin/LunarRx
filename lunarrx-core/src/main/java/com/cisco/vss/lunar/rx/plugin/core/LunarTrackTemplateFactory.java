package com.cisco.vss.lunar.rx.plugin.core;

public class LunarTrackTemplateFactory {
	public static LunarTrack getTrackTemplate(Class<? extends TrackItem> clazz) {
		final String name    = clazz.getName();
		final String terms[] = name.split("\\.");
		return new LunarTrack(null, terms[terms.length-2].toLowerCase(), terms[terms.length-1].toLowerCase());
	}
}
