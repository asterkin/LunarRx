package com.cisco.vss.lunar.rx.scala.samples

import com.cisco.vss.lunar.rx.plugin.Lunar
import rx.lang.scala.JavaConversions._

object LunarBinaryStreamSample {

  def main(args: Array[String]): Unit = {
    val HOST         = args(0)
    val PORT         = Integer.parseInt(args(1))
    val DEVELOPER_ID = "6871c4b35301671668ebf26ae46b6441"
    val lunar        = new Lunar(HOST, PORT, DEVELOPER_ID)
    val SOURCE_ID    = "1"
    val INPUT_PLUGIN = "source_stream"
    val INPUT_TRACK  = "stream"
    val obs          = toScalaObservable(lunar.getInputTrackStream(SOURCE_ID, INPUT_PLUGIN, INPUT_TRACK))
    var count        = 0
    
    println(HOST, PORT)
    obs.map(buf => buf.length).subscribe(
        len => {
          println(count,len)
          count += 1
        }
       ,err => println(err.getMessage())
       ,() => println("Unexpected EOF"))
    println("DONE")
    
  }

}