package com.cisco.vss.lunar.rx.plugin.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.cisco.vss.rx.java.ObjectHolder;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import static com.cisco.vss.lunar.rx.plugin.core.LunarConversions.*;

public class LunarConversionsTest {
	private class TestResponse extends LunarResponse {};
	
	@Test
	public void testCheckResult_OK() {
		final TestResponse               response  = new TestResponse();
		final ObjectHolder<TestResponse> response1 = new ObjectHolder<TestResponse>();
		final ObjectHolder<Throwable>    error     = new ObjectHolder<Throwable>();
		
		response.result = LunarResponse.ResultType.OK;
		checkResult(TestResponse.class)
		.call(response)
		.subscribe(
			new Action1<TestResponse>() {
				@Override
				public void call(final TestResponse t1) {
					response1.value = t1;
				}
			},
			new Action1<Throwable>() {
				@Override
				public void call(final Throwable t1) {
					error.value = t1;
				}
			}
		);
		assertEquals(response, response1.value);
		assertNull(error.value);
	}

	@Test
	public void testCheckResult_ERROR() {
		final TestResponse               response  = new TestResponse();
		final ObjectHolder<TestResponse> response1 = new ObjectHolder<TestResponse>();
		final ObjectHolder<Throwable>    error     = new ObjectHolder<Throwable>();
		
		response.result  = LunarResponse.ResultType.NOT_OK;
		response.message = "errr";
		checkResult(TestResponse.class)
		.call(response)
		.subscribe(
			new Action1<TestResponse>() {
				@Override
				public void call(final TestResponse t1) {
					response1.value = t1;
				}
			},
			new Action1<Throwable>() {
				@Override
				public void call(final Throwable t1) {
					error.value = t1;
				}
			}
		);
		assertNull(response1.value);
		assertEquals("Lunar Response is NOT OK: "+response.message, error.value.getMessage());
	}
	
	@Test
	public void testStatusUpdate2Notify_ADD() {
		final LunarSource                     source  = new LunarSource();
		final LunarSource.StatusUpdateMessage message = source.new StatusUpdateMessage();
		final List<LunarNotify<LunarSource>>  result  = new ArrayList<LunarNotify<LunarSource>>();
		
		message.status = LunarStatusUpdateMessage.Status.UP;
		message.list   = new LunarSource[]{new LunarSource()};		
		statusUpdate2Notify(LunarSource.StatusUpdateMessage.class, LunarSource.class).call(message)
		.subscribe(
			new Action1<LunarNotify<LunarSource>>() {
				@Override
				public void call(final LunarNotify<LunarSource> t1) {
					result.add(t1);
				}}
		);
		assertEquals(1, result.size());
		assertTrue(result.get(0) instanceof LunarAdd<?>);
		assertEquals(message.list[0], result.get(0).getItem());
	}

	@Test
	public void testStatusUpdate2Notify_REMOVE() {
		final LunarSource                     source  = new LunarSource();
		final LunarSource.StatusUpdateMessage message = source.new StatusUpdateMessage();
		final List<LunarNotify<LunarSource>>  result  = new ArrayList<LunarNotify<LunarSource>>();
		
		message.status = LunarStatusUpdateMessage.Status.DOWN;
		message.list   = new LunarSource[]{new LunarSource()};		
		statusUpdate2Notify(LunarSource.StatusUpdateMessage.class, LunarSource.class).call(message)
		.subscribe(
			new Action1<LunarNotify<LunarSource>>() {
				@Override
				public void call(final LunarNotify<LunarSource> t1) {
					result.add(t1);
				}}
		);
		assertEquals(1, result.size());
		assertTrue(result.get(0) instanceof LunarRemove<?>);
		assertEquals(message.list[0], result.get(0).getItem());
	}
	
	@Test
	public void testPrematureRemove() {
		final Func1<LunarNotify<LunarSource>, Boolean> filter = prematureRemove(LunarSource.class);
		assertTrue(filter.call(new LunarAdd<LunarSource>(new LunarSource(1, "source1"))));
		assertFalse(filter.call(new LunarRemove<LunarSource>(new LunarSource(2, "source2"))));
		assertFalse(filter.call(new LunarAdd<LunarSource>(new LunarSource(2, "source2"))));
		assertTrue(filter.call(new LunarRemove<LunarSource>(new LunarSource(1, "source1"))));
	}
	
	@Test
	public void testPluginTrackFilter() {
		final LunarNotify<LunarTrack>  notify = new LunarAdd<LunarTrack>(new LunarTrack(1,"pluginA","trackB"));
		
		assertTrue(pluginTrack(new LunarTrack(1, null, null)).call(notify));
		assertFalse(pluginTrack(new LunarTrack(2, null, null)).call(notify));

		assertTrue(pluginTrack(new LunarTrack(null, "pluginA", null)).call(notify));
		assertFalse(pluginTrack(new LunarTrack(null, "pluginX", null)).call(notify));

		assertTrue(pluginTrack(new LunarTrack(null, null, "trackB")).call(notify));
		assertFalse(pluginTrack(new LunarTrack(null, null, "trackY")).call(notify));
		
	}
}
