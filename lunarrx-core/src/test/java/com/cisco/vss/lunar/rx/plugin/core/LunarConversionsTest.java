package com.cisco.vss.lunar.rx.plugin.core;

import static org.junit.Assert.*;

import org.junit.Test;

import rx.functions.Func1;
import static com.cisco.vss.lunar.rx.plugin.core.LunarConversions.*;

public class LunarConversionsTest {
	@Test
	public void testPrematureRemove() {
		final Func1<LunarNotify<LunarSource>, Boolean> filter = prematureRemove(LunarSource.class);
		assertTrue(filter.call(new LunarAddTrack<LunarSource>(new LunarSource(1, "source1"))));
		assertFalse(filter.call(new LunarRemoveTrack<LunarSource>(new LunarSource(2, "source2"))));
		assertFalse(filter.call(new LunarAddTrack<LunarSource>(new LunarSource(2, "source2"))));
		assertTrue(filter.call(new LunarRemoveTrack<LunarSource>(new LunarSource(1, "source1"))));
	}
	
	@Test
	public void testPluginTrackFilter() {
		final LunarNotify<LunarTrack>  notify = new LunarAddTrack<LunarTrack>(new LunarTrack(1,"pluginA","trackB"));
		
		assertTrue(pluginTrack(new LunarTrack(1, null, null)).call(notify));
		assertFalse(pluginTrack(new LunarTrack(2, null, null)).call(notify));

		assertTrue(pluginTrack(new LunarTrack(null, "pluginA", null)).call(notify));
		assertFalse(pluginTrack(new LunarTrack(null, "pluginX", null)).call(notify));

		assertTrue(pluginTrack(new LunarTrack(null, null, "trackB")).call(notify));
		assertFalse(pluginTrack(new LunarTrack(null, null, "trackY")).call(notify));
		
	}
}
