package com.cisco.vss.lunar.rx.plugin.core;

import static org.junit.Assert.*;
import org.junit.Test;
import rx.functions.Func1;
import static com.cisco.vss.lunar.rx.plugin.core.LunarConversions.*;

public class LunarConversionsTest {
	@Test
	public void testPrematureRemove() {
		final Func1<LunarNotify<LunarSource>, Boolean> filter = prematureRemove(LunarSource.class);
		assertTrue(filter.call(new LunarAdd<LunarSource>(new LunarSource(1, "source1"))));
		assertFalse(filter.call(new LunarRemove<LunarSource>(new LunarSource(2, "source2"))));
		assertFalse(filter.call(new LunarAdd<LunarSource>(new LunarSource(2, "source2"))));
		assertTrue(filter.call(new LunarRemove<LunarSource>(new LunarSource(1, "source1"))));
	}
}
