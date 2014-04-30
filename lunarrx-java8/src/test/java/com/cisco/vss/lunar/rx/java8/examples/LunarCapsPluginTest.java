package com.cisco.vss.lunar.rx.java8.examples;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import com.cisco.vss.lunar.rx.plugin.schema.subtitletext.Subtitles;
import com.cisco.vss.lunar.rx.plugin.schema.subtitletext.Subtitles.*;
import com.cisco.vss.lunar.rx.plugin.schema.capsrx.Caps;

import static com.cisco.vss.lunar.rx.java8.examples.LunarCapsPlugin.*;
import rx.Observable;

public class LunarCapsPluginTest {

	@Test
	public void test() {
		final Subtitles    subtitles = new Subtitles();
		final SubtitleLine line1     = subtitles.new SubtitleLine("Xxxx yyyy");
		final SubtitleLine line2     = subtitles.new SubtitleLine("xxxx Yyyy");
		final SubtitleLine line3     = subtitles.new SubtitleLine("\n");
		final Data         data      = subtitles.new Data();

		data.lines    = new SubtitleLine[]{line1, line2, line3};
		subtitles.data= data;

		final Iterator<Caps> it     = buildCaps(Observable.from(subtitles)).toBlockingObservable().toIterable().iterator();
		final String[]       result = it.next().caps;
		
		assertFalse(it.hasNext());
		assertArrayEquals(new String[]{"Xxxx", "Yyyy"}, result);
	}

}
