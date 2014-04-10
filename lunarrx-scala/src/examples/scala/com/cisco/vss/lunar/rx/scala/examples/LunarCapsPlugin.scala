package com.cisco.vss.lunar.rx.scala.examples

import com.cisco.vss.lunar.rx.plugin.scala._
import com.cisco.vss.lunar.rx.plugin.schema.subtitletext._
import com.cisco.vss.lunar.rx.plugin.schema.capsrx._
import rx.lang.scala._

class LunarCapsPlugin(lunar: Lunar) extends LunarTrackItemStreamTransformer[Subtitles, Caps] (lunar, classOf[Subtitles], classOf[Caps]){

  def makeCaps(caps: Array[String]): Caps = new Caps(0, null, null, null, 0, caps)
  
  @Override
  def transformA(input: Observable[Subtitles]): Observable[Caps] = {
    input
    .map(sub => sub.getText())
    .map(text => text.split("[ .,?!']")) //TODO: regex
    .map(words => words.filter(word => word.length() > 0 && Character.isUpperCase(word(0))))
    .map(caps => makeCaps(caps))
  }
}

object LunarCapsPlugin {
  	def main(args: Array[String]): Unit = {
	  	val HOST         = args(0)
	    val PORT         = Integer.parseInt(args(1))
	    val DEVELOPER_ID = "6871c4b35301671668ebf26ae46b6441";
	    val lunar        = Lunar(HOST, PORT, DEVELOPER_ID)
	    val plugin       = new LunarCapsPlugin(lunar)
	    
	    plugin.run
  	}
}