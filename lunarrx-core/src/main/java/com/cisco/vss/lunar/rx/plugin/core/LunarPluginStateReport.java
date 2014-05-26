package com.cisco.vss.lunar.rx.plugin.core;

class LunarPluginStateReport {
	enum State {
		starting,
		running,
		stopping,
		stopped,
		error
	}

	final String  developerID;
	final String  pluginName;
	final String  sourceID;
	final String  message;
	final State   state;
	
	private LunarPluginStateReport(final String developerID, final String pluginName, final Integer sourceID, final String message, final State state) {
		this.developerID = developerID;
		this.pluginName  = pluginName;
		this.sourceID    = sourceID.toString();
		this.message     = message;
		this.state       = state;
	}

	@Override
	public boolean equals(final Object object) {
		if(null == object) return false;
		if(!(object instanceof LunarPluginStateReport)) return false;
		final LunarPluginStateReport that = (LunarPluginStateReport)object;
		return (this.developerID.equals(that.developerID))
				&& (this.pluginName.equals(that.pluginName))
				&& (this.sourceID.equals(that.sourceID))
				&& ((this.message == null && that.message == null) || (this.message.equals(that.message))
				&& (this.state.equals(that.state)));
	}
	
	static LunarPluginStateReport stopping(final String developerID, final LunarTrack track) {
		return new LunarPluginStateReport(developerID, track.pluginName, track.sourceID, track.trackName, State.stopping);
	}

	private static String formatMessage(final LunarTrack track, final Throwable err) {
		return String.format("%s - Error: %s", track.trackName, err.getMessage());
	}
	
	static LunarPluginStateReport error(final String developerID, final LunarTrack track, final Throwable err) {
		return new LunarPluginStateReport(developerID, track.pluginName, track.sourceID, formatMessage(track, err), State.error);		
	}
	
	static LunarPluginStateReport stopping(final String developerID, final LunarTrack track, final Throwable err) {
		return new LunarPluginStateReport(developerID, track.pluginName, track.sourceID, formatMessage(track, err), State.stopping);
	}
	
	static LunarPluginStateReport stopped(final String developerID, final LunarTrack track) {
		return new LunarPluginStateReport(developerID, track.pluginName, track.sourceID, track.trackName, State.stopped);
	}

	static LunarPluginStateReport stopped(final String developerID, final LunarTrack track, final Throwable err) {
		return new LunarPluginStateReport(developerID, track.pluginName, track.sourceID, formatMessage(track, err), State.stopped);
	}
	
	static LunarPluginStateReport starting(final String developerID, final LunarTrack track) {
		return new LunarPluginStateReport(developerID, track.pluginName, track.sourceID, track.trackName, State.starting);
	}

	static LunarPluginStateReport running(final String developerID, final LunarTrack track) {
		return new LunarPluginStateReport(developerID, track.pluginName, track.sourceID, track.trackName, State.running);
	}		
}
