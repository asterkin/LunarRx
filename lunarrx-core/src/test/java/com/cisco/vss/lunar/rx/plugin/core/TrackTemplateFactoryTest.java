package com.cisco.vss.lunar.rx.plugin.core;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cisco.vss.lunar.rx.plugin.core.testplugin.TestTrack;

import static com.cisco.vss.lunar.rx.plugin.core.LunarTrackTemplateFactory.*;

public class TrackTemplateFactoryTest {

	@Test
	public void testGetTrackTemplate() {
		final LunarTrack template = getTrackTemplate(TestTrack.class);
		assertNull(template.sourceID);
		assertEquals("testplugin", template.pluginName);
		assertEquals("testtrack" , template.trackName);
	}

}
