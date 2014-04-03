package com.cisco.vss.lunar.rx.plugin.core;

import rx.schedulers.Schedulers;

public class LunarPluginStateReporter {
	protected final Lunar        lunar;
	protected final String       developerID;
	
	public LunarPluginStateReporter(final Lunar lunar, final String developerID) {
		this.lunar       = lunar;
		this.developerID = developerID;		
	}

	private void reportStatus(final LunarPluginStateReport report) {
		lunar.sendReport(report)
		.subscribeOn(Schedulers.newThread()) //TODO: quasars
		.subscribe();		
	}
	
	public void starting(final LunarTrack track) {
		reportStatus(LunarPluginStateReport.starting(developerID, track));
	}

	public void running(final LunarTrack track) {
		reportStatus(LunarPluginStateReport.running(developerID, track));
	}

	public void stopping(final LunarTrack track) {
		reportStatus(LunarPluginStateReport.stopping(developerID, track));
	}

	public void stopping(final LunarTrack track, final Throwable err) {
		reportStatus(LunarPluginStateReport.stopping(developerID, track, err));
	}
	
	public void stopped(final LunarTrack track) {
		reportStatus(LunarPluginStateReport.stopped(developerID, track));
	}

	public void stopped(final LunarTrack track, final Throwable err) {
		reportStatus(LunarPluginStateReport.stopped(developerID, track, err));
	}
}
