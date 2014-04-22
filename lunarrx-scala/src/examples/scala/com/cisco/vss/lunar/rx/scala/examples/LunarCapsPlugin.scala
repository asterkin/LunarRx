package com.cisco.vss.lunar.rx.scala.examples

import com.cisco.vss.lunar.rx.plugin.scala._
import com.cisco.vss.lunar.rx.plugin.schema.subtitletext._
import com.cisco.vss.lunar.rx.plugin.schema.capsrx._
import rx.lang.scala._

object LunarCapsPlugin {
	def buildCaps(input: Observable[Subtitles]): Observable[Caps] = {
		input
		.map(sub   => sub.getText)
		.map(text  => text.split("\\s+"))
		.map(words => words.map(   word => word.trim)
		                   .filter(word => !word.isEmpty)
		                   .filter(word => Character.isUpperCase(word(0))))
		.map(caps  => new Caps(caps))
	}
  
  	def main(args: Array[String]): Unit = {
	    Lunar(args).transform(classOf[Subtitles], buildCaps _, classOf[Caps])	    
  	}
}