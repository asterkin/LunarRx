package com.cisco.vss.lunar.rx.scala.plugin
import com.cisco.vss.lunar.rx.plugin.core
import com.cisco.vss.lunar.rx.plugin.core.{LunarTrack, LunarTrackItem}
import rx.lang.scala.JavaConversions._
import rx.lang.scala._
import java.net.URL
import com.cisco.vss.rx.java._
import scala.reflect._

abstract class Lunar {
	private [scala] val asJavaLunar: core.Lunar

	def transform(sourceTrackTemplate: LunarTrack, trans: (Observable[Array[Byte]], LunarTrack) => Observable[_ <: Array[Byte]], resultTrackTemplate: LunarTrack) : Unit = {
	  val javaTrans = new rx.functions.Func2[rx.Observable[Array[Byte]], LunarTrack, rx.Observable[_ <: Array[Byte]]]() {
		@Override
		def call(input: rx.Observable[Array[Byte]], track: LunarTrack) : rx.Observable[_ <: Array[Byte]] = {
			val result     = trans(toScalaObservable(input), track)
			val javaResult = toJavaObservable(result) 
			return javaResult
		}
	  }
	  
	  asJavaLunar.transform(sourceTrackTemplate, javaTrans, resultTrackTemplate)  
	}
	
	def transform[R <: LunarTrackItem : ClassTag](sourceTrackTemplate: LunarTrack, trans: (Observable[Array[Byte]]) => Observable[_ <: R]): Unit = {
	  val resultType: Class[R] = classTag[R].runtimeClass match {case t : (Class[R] @unchecked) => t} //TODO
	  val javaTrans = new rx.functions.Func1[rx.Observable[Array[Byte]], rx.Observable[_ <: R]]() {
		@Override
		def call(input: rx.Observable[Array[Byte]]) : rx.Observable[_ <: R] = {
			val result     = trans(toScalaObservable(input))
			val javaResult = toJavaObservable(result) 
			return javaResult
		}
	  }
	  asJavaLunar.transform[R](sourceTrackTemplate, javaTrans, resultType)
	}
	
	def transform[T <: LunarTrackItem : ClassTag, R <: LunarTrackItem : ClassTag](trans: (Observable[T]) => Observable[R]): Unit = {
	  val sourceType: Class[T] = classTag[T].runtimeClass match {case t : (Class[T] @unchecked) => t} //TODO
	  val resultType: Class[R] = classTag[R].runtimeClass match {case t : (Class[R] @unchecked) => t} //TODO
	  val javaTrans = new rx.functions.Func1[rx.Observable[T], rx.Observable[_ <: R]]() {
		@Override
		def call(input: rx.Observable[T]) : rx.Observable[_ <: R] = {
			val result     = trans(toScalaObservable(input))
			val javaResult = toJavaObservable(result) 
			return javaResult
		}
	  }
	  asJavaLunar.transform[T,R](sourceType, javaTrans, resultType)
	}

	
	//TODO: add more useful converters (httpPost, etc.)
	def synchHttpGet(url: URL): Observable[String] = {
		toScalaObservable(Conversions.synchHttpGet.call(url))
	}
	
	//TODO: api for applications
}

object Lunar {
  def apply(args: Array[String]): Lunar = new Lunar {
	  val asJavaLunar = new core.Lunar(args)
  }
}

