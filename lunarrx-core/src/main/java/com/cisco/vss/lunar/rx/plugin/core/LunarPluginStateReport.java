package com.cisco.vss.lunar.rx.plugin.core;

public class LunarPluginStateReport {
	enum State {
		starting,
		running,
		stopping,
		stopped
	}

	final String  developerID;
	final String  pluginName;
	final Integer sourceID;
	final String  message;
	final State   state;
	
	private LunarPluginStateReport(final String developerID, final String pluginName, final Integer sourceID, final String message, final State state) {
		this.developerID = developerID;
		this.pluginName  = pluginName;
		this.sourceID    = sourceID;
		this.message     = message;
		this.state       = state;
	}

	public static LunarPluginStateReport stopping(final String developerID, final LunarTrack track) {
		return new LunarPluginStateReport(developerID, track.pluginName, track.sourceID, track.trackName, State.stopping);
	}

	private static String formatMessage(final LunarTrack track, final Throwable err) {
		return String.format("%s - Error: %s", track.trackName, err.getMessage());
	}
	
	public static LunarPluginStateReport stopping(final String developerID, final LunarTrack track, final Throwable err) {
		return new LunarPluginStateReport(developerID, track.pluginName, track.sourceID, formatMessage(track, err), State.stopping);
	}
	
	public static LunarPluginStateReport stopped(final String developerID, final LunarTrack track) {
		return new LunarPluginStateReport(developerID, track.pluginName, track.sourceID, track.trackName, State.stopped);
	}

	public static LunarPluginStateReport stopped(final String developerID, final LunarTrack track, final Throwable err) {
		return new LunarPluginStateReport(developerID, track.pluginName, track.sourceID, formatMessage(track, err), State.stopped);
	}
	
	public static LunarPluginStateReport starting(final String developerID, final LunarTrack track) {
		return new LunarPluginStateReport(developerID, track.pluginName, track.sourceID, track.trackName, State.starting);
	}

	public static LunarPluginStateReport running(final String developerID, final LunarTrack track) {
		return new LunarPluginStateReport(developerID, track.pluginName, track.sourceID, track.trackName, State.running);
	}		
}