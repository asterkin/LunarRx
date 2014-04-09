package com.cisco.vss.lunar.rx.scala.examples

import com.cisco.vss.lunar.rx.plugin.scala._
import com.cisco.vss.lunar.rx.plugin.schema._
import rx.lang.scala._

class LunarCapsPlugin(lunar: Lunar) extends LunarTrackItemStreamTransformer[SubtitlesTrackItem, CapsTrackItem] (lunar){

  @Override
  def transformA(input: Observable[SubtitlesTrackItem]): Observable[CapsTrackItem] = {
    null
  }
}