package com.cisco.vss.lunar.rx.scala.examples

import com.cisco.vss.lunar.rx.plugin.scala._
import rx.lang.scala.JavaConversions._
import scala.collection.immutable.HashMap
import scala.util.control.Breaks._
import com.cisco.vss.lunar.rx.scala.mpeg._

object LunarMpeg2PatPlugin {
  def main(args: Array[String]): Unit = {
    val HOST         = args(0)
    val PORT         = Integer.parseInt(args(1))
    val DEVELOPER_ID = "6871c4b35301671668ebf26ae46b6441"
    val lunar        = Lunar(HOST, PORT, DEVELOPER_ID)
    val SOURCE_ID    = "1"
    val INPUT_PLUGIN = "source_stream"
    val INPUT_TRACK  = "stream"
    val ts           = lunar.getInputTrackStream(SOURCE_ID, INPUT_PLUGIN, INPUT_TRACK)
    
    ts
    .map(p => TsPacket(p))
    .filter(tsp => !tsp.hasError)
    .filter(tsp => tsp.hasPayloadStart)
    .filter(tsp => 0 == tsp.pid)
    .map(tsp => PsiPat(tsp))
    .distinctUntilChanged
    //TODO: write Pat back to Lunar?
    .subscribe(
        pat => {
          println("New version of PAT")
          pat.asObservable.subscribe(
              entry => println("0x%04X -> 0x%04X".format(entry.programNumber, entry.pid)) 
          )
        }
        ,err => println(err.getMessage())
       ,() => println("Unexpected EOF")        
    )    
  }
}
