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

	static LunarPluginStateReport starting(String developerID, String pluginName, Integer sourceID, String message) {
		return new LunarPluginStateReport(developerID, pluginName, sourceID, message, State.starting);
	}
	
	static LunarPluginStateReport running(String developerID, String pluginName, Integer sourceID, String message) {
		return new LunarPluginStateReport(developerID, pluginName, sourceID, message, State.running);
	}
	
	static LunarPluginStateReport stopping(String developerID, String pluginName, Integer sourceID, String message) {
		return new LunarPluginStateReport(developerID, pluginName, sourceID, message, State.stopping);
	}
	
	static LunarPluginStateReport stopped(String developerID, String pluginName, Integer sourceID, String message) {
		return new LunarPluginStateReport(developerID, pluginName, sourceID, message, State.stopped);
	}		
}
