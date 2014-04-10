package com.cisco.vss.lunar.rx.plugin.scala
import com.cisco.vss.lunar.rx.plugin._
import rx.lang.scala.JavaConversions._
import rx.lang.scala._
import java.net.URL
import com.cisco.vss.rx.java._
import com.cisco.vss.lunar.rx.plugin.core.TrackItem
import com.cisco.vss.lunar.rx.plugin.core.LunarTrack

abstract class Lunar {
	private [scala] val asJavaLunar: core.Lunar

	def transform(sourceTrackTemplate: LunarTrack, trans: (Observable[Array[Byte]], LunarTrack) => Observable[Array[Byte]], resultTrackTemplate: LunarTrack) : Unit = {
	  val plugin: core.LunarByteStreamTransformer =  new core.LunarByteStreamTransformer(asJavaLunar, sourceTrackTemplate, resultTrackTemplate) {
		@Override
		def transform(input: rx.Observable[Array[Byte]], track: LunarTrack) : rx.Observable[_ <: Array[Byte]] = {
			val result     = trans(toScalaObservable(input), track)
			val javaResult = toJavaObservable(result) 
			return javaResult
		}      
      } 
	  
	  plugin.run
	}
	
	def transform[R <: TrackItem](sourceTrackTemplate: LunarTrack, trans: (Observable[Array[Byte]]) => Observable[R] ,resultType: Class[R]): Unit = {
		val plugin: core.LunarTrackItemStreamGenerator[R] = new core.LunarTrackItemStreamGenerator[R](
		    asJavaLunar, 
		    sourceTrackTemplate,
		    resultType
		){
	      @Override
	      def generateR(input: rx.Observable[Array[Byte]]) : rx.Observable[_ <: R] = {
	        val result     = trans(toScalaObservable(input))
	        val javaResult = toJavaObservable(result) 
	        return javaResult
	      }
		}
		
		plugin.run
	}
	
	def transform[T <: TrackItem, R <: TrackItem](sourceType: Class[T], trans: (Observable[T]) => Observable[R] ,resultType: Class[R]): Unit = {
	   val plugin: core.LunarTrackItemStreamTransformer[T, R] = new core.LunarTrackItemStreamTransformer[T, R](asJavaLunar, sourceType, resultType) {
	     @Override
        def transformT(input: rx.Observable[T]) : rx.Observable[_ <: R] = {
          val result     = trans(toScalaObservable(input))
          val javaResult = toJavaObservable(result) 
          return javaResult
        }
      }
	   plugin.run()
	}
	
	//TODO: add more useful converters (httpPost, etc.)
	def synchHttpGet(url: URL): Observable[String] = {
		toScalaObservable(Conversions.synchHttpGet.call(url))
	}
	
	//TODO: api for applications
}

object Lunar {
  def apply(args: Array[String]): Lunar = {
	val DEVELOPER_ID = args(0);
	// args(1) - plugin name
    val HOST         = args(2)
	val PORT         = Integer.parseInt(args(3))
	// arg(4) - sourceID
	new Lunar {
	  val asJavaLunar = new core.Lunar(HOST, PORT, DEVELOPER_ID)
	}
  }
}

