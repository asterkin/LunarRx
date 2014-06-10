package com.cisco.vss.lunar.rx.scala
import java.io._
import rx.lang.scala._
import scala.sys._
import scala.sys.process._

package object plugin {
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

  def readText(is: InputStream): String = {
    val rd: BufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8")) 
    val builder = new StringBuilder()    
    try {
      var line = rd.readLine 
      while (line != null) { 
        builder.append(line + "\n")
        line = rd.readLine
      }
    } finally {
      rd.close
    }
    builder.toString    
  }

  def hasData(reader: BufferedInputStream): Boolean = {
    for(i <- 0 to 1000) {
      if(reader.available() > 0) return true
      Thread.sleep(100)
    }
    false
  }
  
  def waitForPattern(reader : BufferedInputStream, pattern: List[Byte]): BufferedInputStream = {
  	def waitForTail(reader : BufferedInputStream, pattern: List[Byte]): Boolean = {
  		pattern match {
  			case Nil   => true
  			case x::xs => {val c = reader.read(); if(x == c) waitForTail(reader, xs) else false}
  		}
  	}
  	val b = reader.read
  	if (-1 == b) reader
  	else if (pattern.head == b && waitForTail(reader, pattern.tail)) reader
  	else waitForPattern(reader, pattern)
  }                                               
  
  def writeBuf[T](input: Observable[Array[Byte]], os: OutputStream, observer: Subscriber[T]): Unit = {
    input.subscribe(
        buf => {
          os.write(buf)
          os.flush
        },
        err => observer.onError(err),
        () => observer.onCompleted()
    )
  }

  def readStream[T](cmdName: String, is: InputStream, readF: (BufferedInputStream)=>T, observer: Subscriber[T]): Unit = {
    val reader = new BufferedInputStream(is)
	while(!Thread.interrupted() && hasData(reader))
	   observer.onNext(readF(reader))	              
	if(!Thread.interrupted()) {
		observer.onError(new Exception(cmdName + " is stuck or down"))
	    Thread.currentThread().interrupt()
	}    
  }

  def launchProcess[T](cmd: String, input: Observable[Array[Byte]], readF: (BufferedInputStream)=>T): Observable[T] = {
    val terms = cmd.split(" ")
    val cmdName = if (terms.length > 0) terms(0) else cmd
    Observable[T]{observer => 
	    try {
	      val io   = new ProcessIO(
	          {os  => writeBuf(input, os, observer) },
	          {is  => readStream(cmdName, is, readF, observer) },
	          {err => observer.onError(new Exception(readText(err)))}
	      )
	      val proc = Process(Array("/bin/bash", "-c", cmd)).run(io)
	      addShutdownHook { proc.destroy }
	      Subscription { proc.destroy } //not sure we need the both
	    } catch {
	      case err: IOException =>  {
	        println("Failed to launch "+cmdName+": ", err)
	        exit(1)
	      }
	    }
    }
  } 
  
}