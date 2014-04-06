package com.cisco.vss.lunar.rx.plugin.core;


import org.junit.Test;
import static org.mockito.Mockito.*; 
import org.junit.Before;  
import org.junit.After;  
import org.junit.runner.RunWith;  
import org.mockito.Mock;  
import org.mockito.runners.MockitoJUnitRunner;
import rx.Observable;

@RunWith(MockitoJUnitRunner.class)
public class LunarPluginStateReporterTest {
	final static String     DEVELOPER_ID = "6871c4b35301671668ebf26ae46b6441";
	final static LunarTrack TRACK        = new LunarTrack(1, "plugin1", "track1");
	final static Throwable  ERROR        = new Exception("Error");

	@Mock
	Lunar                     lunarMock;
	Observable<LunarResponse> obsMock = Observable.from(new LunarResponse());
	LunarPluginStateReporter  reporter;

	@Before  
    public void setUp() {
		reporter = new LunarPluginStateReporter(lunarMock, DEVELOPER_ID);
	}

	@After
	public void tearDown() {
		reporter = null;
	}
	
	@Test
	public void testStarting() {
//		when(lunarMock.sendReport(LunarPluginStateReport.starting(DEVELOPER_ID, TRACK))).thenReturn(obsMock);
		reporter.starting(TRACK);
	}
	
	@Test
	public void testRunning() {
//		when(lunarMock.sendReport(LunarPluginStateReport.running(DEVELOPER_ID, TRACK))).thenReturn(obsMock);
		reporter.running(TRACK);
	}

	@Test
	public void testStopping() {
//		when(lunarMock.sendReport(LunarPluginStateReport.stopping(DEVELOPER_ID, TRACK))).thenReturn(obsMock);
		reporter.stopping(TRACK);
	}

	@Test
	public void testStoppingError() {
//		when(lunarMock.sendReport(LunarPluginStateReport.stopping(DEVELOPER_ID, TRACK, ERROR))).thenReturn(obsMock);
		reporter.stopping(TRACK, ERROR);
	}

	@Test
	public void testStopped() {
//		when(lunarMock.sendReport(LunarPluginStateReport.stopped(DEVELOPER_ID, TRACK))).thenReturn(obsMock);
		reporter.stopped(TRACK);
	}

	@Test
	public void testStoppedError() {
//		when(lunarMock.sendReport(LunarPluginStateReport.stopped(DEVELOPER_ID, TRACK, ERROR))).thenReturn(obsMock);
		reporter.stopped(TRACK, ERROR);
	}
	
}
