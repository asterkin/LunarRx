package com.cisco.vss.lunar.rx.plugin.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class LunarPluginTrackFilterTest {

	@Test
	public void testEquals() {
		assertTrue(new LunarPluginTrackFilter("pluginA", "trackB").call(new LunarAdd<LunarTrack>(new LunarTrack("pluginA", "trackB"))));
	}

	@Test
	public void testNotEqualPlugin() {
		assertFalse(new LunarPluginTrackFilter("pluginA", "trackB").call(new LunarAdd<LunarTrack>(new LunarTrack("pluginC", "trackB"))));
	}

	@Test
	public void testNotEqualTrack() {
		assertFalse(new LunarPluginTrackFilter("pluginA", "trackB").call(new LunarAdd<LunarTrack>(new LunarTrack("pluginA", "trackC"))));
	}
	
}
