package com.cisco.vss.lunar.rx.scala.examples

import com.cisco.vss.lunar.rx.plugin.scala._
import com.cisco.vss.lunar.rx.plugin.schema.subtitletext._
import com.cisco.vss.lunar.rx.plugin.schema.capsrx._
import rx.lang.scala._

object LunarCapsPlugin {
	def buildCaps(input: Observable[Subtitles]): Observable[Caps] = {
		input
		.map(sub   => sub.getText())
		.map(text  => text.split("[ .,?!']")) //TODO: regex
		.map(words => words.filter(word => word.length() > 0 && Character.isUpperCase(word(0))))
		.map(caps  => new Caps(caps))
	}
  
  	def main(args: Array[String]): Unit = {
	    Lunar(args).transform(classOf[Subtitles], buildCaps, classOf[Caps])	    
  	}
}