package com.cisco.vss.lunar.rx.plugin.scala
import com.cisco.vss.lunar.rx.plugin._
import rx.lang.scala.JavaConversions._
import rx.lang.scala._

abstract class Lunar {
	private [scala] val asJavaLunar: core.Lunar
	
	def getInputTrackStream(sourceID: String, pluginName: String, trackName: String): Observable[Array[Byte]] =
		toScalaObservable(asJavaLunar.getInputTrackStream(sourceID,pluginName, trackName))

	def getInputTrackItemStream[T](sourceID: String, pluginName: String, trackName: String)(implicit m:Manifest[T]): Observable[T] = {
		val clazz = m.runtimeClass.asInstanceOf[Class[T]]
		toScalaObservable(asJavaLunar.getInputTrackItemStream(clazz, sourceID,pluginName, trackName))
	}
}

object Lunar {
  def apply(host: String, port: Integer, developerID: String): Lunar =  new Lunar {
    val asJavaLunar = new core.Lunar(host, port, developerID)
  } 
}