package com.cisco.vss.lunar.rx.scala.examples

import com.cisco.vss.lunar.rx.plugin.scala.Lunar
import com.cisco.vss.lunar.rx.plugin.schema.SubtitlesTrackItem

object LunarWatchAndLearnPlugin {
  def main(args: Array[String]): Unit = {
    val HOST         = args(0)
    val PORT         = Integer.parseInt(args(1))
    val DEVELOPER_ID = "6871c4b35301671668ebf26ae46b6441"
    val lunar        = Lunar(HOST, PORT, DEVELOPER_ID)
    val SOURCE_ID    = "1"
    val INPUT_PLUGIN = "subtitletext"
    val INPUT_TRACK  = "subtitles"
    val ts           = lunar.getInputTrackItemStream(classOf[SubtitlesTrackItem], SOURCE_ID, INPUT_PLUGIN, INPUT_TRACK)
    
    ts
//    .map(p => TsPacket(p))
//    .filter(tsp => !tsp.hasError)
//    .filter(tsp => tsp.hasPayloadStart)
//    .filter(tsp => 0 == tsp.pid)
//    .map(tsp => PsiPat(tsp))
//    .distinctUntilChanged
//    //TODO: write Pat back to Lunar?
    .subscribe(
        sub => {
          println(sub.getTime(), sub.getPts())
          println(sub.getText())
        }
        ,err => {
          println("Error!",err)
        }
       ,() => println("Unexpected EOF")        
    )    
  }
}