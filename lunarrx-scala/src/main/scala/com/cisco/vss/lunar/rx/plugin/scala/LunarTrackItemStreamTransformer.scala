package com.cisco.vss.lunar.rx.plugin.scala

import com.cisco.vss.lunar.rx.plugin._
import rx.lang.scala.JavaConversions._
import rx.lang.scala._
import com.cisco.vss.lunar.rx.plugin.core.TrackItem

abstract class LunarTrackItemStreamTransformer[T <: TrackItem, R <: TrackItem] (lunar: Lunar) {
  private [scala] val asJava: core.LunarTrackItemStreamTransformer[T, R] = new core.LunarTrackItemStreamTransformer[T, R](lunar.asJavaLunar) {
    @Override
    def transformT(input: rx.Observable[T]) : rx.Observable[_ <: R] = {
        val result     = transformA(toScalaObservable(input))
        val javaResult = toJavaObservable(result) 
        return javaResult
    }
  } 

  //Name?!!!!
  def transformA(input: Observable[T]): Observable[R]
}