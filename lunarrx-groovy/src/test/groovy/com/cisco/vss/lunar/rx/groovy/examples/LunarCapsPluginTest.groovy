package com.cisco.vss.lunar.rx.groovy.examples;

import static org.junit.Assert.*;
import org.junit.Test;
import com.cisco.vss.lunar.rx.plugin.schema.subtitletext.Subtitles;
import static com.cisco.vss.lunar.rx.groovy.examples.LunarCapsPlugin.*;
import rx.Observable;

class LunarCapsPluginTest {
	@Test
	public void test() {
	  def subtitles = new Subtitles()
	  def line1     = new Subtitles.SubtitleLine(subtitles, "Xxxx yyyy")
	  def line2     = new Subtitles.SubtitleLine(subtitles, "xxxx Yyyy")
	  def line3     = new Subtitles.SubtitleLine(subtitles, "\n")
	  def data      = new Subtitles.Data(subtitles)
	   
	  data.lines    = [line1, line2, line3]
	  subtitles.data= data
	  
	  def result = buildCaps(Observable.from(subtitles)).toBlockingObservable().toIterable().last().caps
	  assert(["Xxxx", "Yyyy"].equals(result))
	}

}
