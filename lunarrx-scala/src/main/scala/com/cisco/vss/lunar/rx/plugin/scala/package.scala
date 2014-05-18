package com.cisco.vss.lunar.rx.plugin
import rx.lang.scala._

package object scala {
	implicit class ObservableOps[T1](val obs: Observable[T1]) extends AnyVal {
	  def reduceUntil[T2](init: T2, func: (T2,T1)=>T2, pred: (T1)=>Boolean): Observable[T2] = {
	    Observable[T2](observer => {
	    	var current: T2 = init
	    	obs.subscribe(
	    	    (v: T1) => {
	    	      current = func(current, v)
	    	      if(pred(v)) {
	    	        observer.onNext(current)
	    	        current = init
	    	      }
	    	    },
	    	    (e: Throwable) => observer.onError(e),
	    	    ()             => observer.onCompleted
	    	)
	    })
	  }
	  
	  def slidingBuffer(): Observable[Tuple2[T1, T1]] = {
	    Observable[Tuple2[T1, T1]](observer => {
	      var prev: Option[T1] = None
	      obs.subscribe(
	         (v: T1) => { 
	           if (prev.nonEmpty) observer.onNext((prev.get, v))
	           prev = Some(v)
	         },
    	     (e: Throwable) => observer.onError(e),
	    	 ()             => observer.onCompleted
	      )
	    })
	  }
	}
}