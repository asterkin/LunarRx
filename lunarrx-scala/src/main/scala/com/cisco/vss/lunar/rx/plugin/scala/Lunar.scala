package com.cisco.vss.lunar.rx.plugin.scala
import com.cisco.vss.lunar.rx.plugin._
import rx.lang.scala.JavaConversions._
import rx.lang.scala._
import java.net.URL
import com.cisco.vss.rx.java._
import com.cisco.vss.lunar.rx.plugin.core.TrackItem

abstract class Lunar {
	private [scala] val asJavaLunar: core.Lunar

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
	
	//TODO: remove from this
	def getInputTrackStream(sourceID: Integer, pluginName: String, trackName: String): Observable[Array[Byte]] =
		toScalaObservable(asJavaLunar.getInputTrackStream(sourceID,pluginName, trackName))

	def getInputTrackItemStream[T](clazz: Class[T], sourceID: Integer, pluginName: String, trackName: String): Observable[T] = {
		toScalaObservable(asJavaLunar.getInputTrackItemStream(clazz, sourceID,pluginName, trackName))
	}
	
	def synchHttpGet(url: URL): Observable[String] = {
		toScalaObservable(Conversions.synchHttpGet.call(url))
	}
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

