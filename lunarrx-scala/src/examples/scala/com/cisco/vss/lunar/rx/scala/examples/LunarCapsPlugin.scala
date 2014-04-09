package com.cisco.vss.lunar.rx.scala.examples

import com.cisco.vss.lunar.rx.plugin.scala._
import com.cisco.vss.lunar.rx.plugin.schema._
import rx.lang.scala._

class LunarCapsPlugin(lunar: Lunar) extends LunarTrackItemStreamTransformer[SubtitlesTrackItem, CapsTrackItem] (lunar, classOf[SubtitlesTrackItem], classOf[CapsTrackItem]){

  @Override
  def transformA(input: Observable[SubtitlesTrackItem]): Observable[CapsTrackItem] = {
    null
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