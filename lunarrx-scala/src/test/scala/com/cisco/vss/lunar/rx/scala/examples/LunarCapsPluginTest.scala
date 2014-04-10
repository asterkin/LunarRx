package com.cisco.vss.lunar.rx.scala.examples

import org.scalatest.junit.JUnitSuite
import org.junit.Test
import org.junit.Assert._
import com.cisco.vss.lunar.rx.plugin.schema.subtitletext.Subtitles
import com.cisco.vss.lunar.rx.scala.examples.LunarCapsPlugin._
import rx.lang.scala._

class LunarCapsPluginTest extends JUnitSuite {
	@Test def testBuildCaps() {
	  val subtitles = new Subtitles
	  val line1     = new subtitles.SubtitleLine("Xxxx yyyy")
	  val line2     = new subtitles.SubtitleLine("xxxx Yyyy")
	  val line3     = new subtitles.SubtitleLine("\n")
	  val data      = new subtitles.Data 
	  data.lines    = Array(line1, line2, line3)
	  subtitles.data= data
	  
	  val result = buildCaps(Observable.from(List(subtitles))).toBlockingObservable.toIterable.last.caps
	 
	  assertTrue(Array("Xxxx", "Yyyy").sameElements(result))
	}
}