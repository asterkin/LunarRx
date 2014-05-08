package com.cisco.vss.lunar.rx.plugin.scala
import rx.lang.scala._

object ObservableEx {
	implicit class ObservableOps[T](val obs: Observable[T]) extends AnyVal {
	  def reduceUntil(init: T, func: (T,T)=>T, pred: (T)=>Boolean): Observable[T] = {
	    Observable[T](observer => {
	    	var current: T = init
	    	obs.subscribe(
	    	    (v: T) => {
	    	      current = func(current, v)
	    	      if(pred(current)) {
	    	        observer.onNext(current)
	    	        current = init
	    	      }
	    	    },
	    	    (e: Throwable) => observer.onError(e),
	    	    ()             => observer.onCompleted
	    	)
	    })
	  }
	}
}
