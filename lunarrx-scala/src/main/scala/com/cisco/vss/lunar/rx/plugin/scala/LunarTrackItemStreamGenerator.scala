package com.cisco.vss.lunar.rx.plugin.scala
import com.cisco.vss.lunar.rx.plugin._
import rx.lang.scala.JavaConversions._
import rx.lang.scala._
import com.cisco.vss.lunar.rx.plugin.core.TrackItem

abstract class LunarTrackItemStreamGenerator[R <: TrackItem] (lunar: Lunar, sourceTrackTemplate: core.LunarTrack, resultType: Class[R]) {
	private [scala] val asJava: core.LunarTrackItemStreamGenerator[R] = new core.LunarTrackItemStreamGenerator[R](
	    lunar.asJavaLunar, 
	    sourceTrackTemplate,
	    resultType
	){
      @Override
      def generateR(input: rx.Observable[Array[Byte]]) : rx.Observable[_ <: R] = {
        val result     = transformA(toScalaObservable(input))
        val javaResult = toJavaObservable(result) 
        return javaResult
      }
	}
	
    def run: Unit = asJava.run()

		//TODO: name!???
	def transformA(input: Observable[Array[Byte]]): Observable[R]

}