package com.cisco.vss.lunar.rx.plugin.scala
import com.cisco.vss.lunar.rx.plugin._
import rx.lang.scala.JavaConversions._
import rx.lang.scala._

abstract class LunarByteStreamTransformer(lunar: Lunar, sourceTrackTemplate: core.LunarTrack, resultTrackTemplate: core.LunarTrack) {
	private [scala] val asJava: core.LunarByteStreamTransformer = new core.LunarByteStreamTransformer(lunar.asJavaLunar, sourceTrackTemplate, resultTrackTemplate) {
      @Override
      def transform(input: rx.Observable[Array[Byte]]) : rx.Observable[_ <: Array[Byte]] = {
        val result     = transformA(toScalaObservable(input))
        val javaResult = toJavaObservable(result) 
        return javaResult
      } 
    } 

	//TODO: name!???
	def transformA(input: Observable[Array[Byte]]): Observable[Array[Byte]]
}

