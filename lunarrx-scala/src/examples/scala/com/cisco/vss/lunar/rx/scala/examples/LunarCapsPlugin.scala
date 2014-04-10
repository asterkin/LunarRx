package com.cisco.vss.lunar.rx.scala.examples

import com.cisco.vss.lunar.rx.plugin.scala._
import com.cisco.vss.lunar.rx.plugin.schema.subtitletext._
import com.cisco.vss.lunar.rx.plugin.schema.capsrx._
import rx.lang.scala._

class LunarCapsPlugin(lunar: Lunar) extends LunarTrackItemStreamTransformer[Subtitles, Caps] (lunar, classOf[Subtitles], classOf[Caps]){

  @Override
  def transformA(input: Observable[Subtitles]): Observable[Caps] = {
    input
    .map(sub   => sub.getText())
    .map(text  => text.split("[ .,?!']")) //TODO: regex
    .map(words => words.filter(word => word.length() > 0 && Character.isUpperCase(word(0))))
    .map(caps  => new Caps(caps))
  }
}

object LunarCapsPlugin {
  	def main(args: Array[String]): Unit = {
	    val lunar        = Lunar(args)
	    val plugin       = new LunarCapsPlugin(lunar)
	    
	    plugin.run
  	}
}