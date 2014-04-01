package com.cisco.vss.lunar.rx.plugin.scala
import com.cisco.vss.lunar.rx.plugin._
import rx.lang.scala.JavaConversions._
import rx.lang.scala._
import java.net.URL
import com.cisco.vss.rx.java._

abstract class Lunar {
	private [scala] val asJavaLunar: core.Lunar
	
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
  def apply(host: String, port: Integer): Lunar =  new Lunar {
    val asJavaLunar = new core.Lunar(host, port)
  } 
}