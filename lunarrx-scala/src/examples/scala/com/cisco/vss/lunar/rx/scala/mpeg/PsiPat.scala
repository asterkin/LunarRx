package com.cisco.vss.lunar.rx.scala.mpeg
import rx.lang.scala._
import scala.collection.mutable.WrappedArray

abstract class PsiPat {
	sealed class Entry(val programNumber: Short, val pid: Short) {
    override def equals(other: Any) = other match {
	    case that: PsiPat#Entry => (that.programNumber == this.programNumber) && (that.pid == this.pid)
	    case _                  => false
	}  
	  
	}
	val table: WrappedArray[Entry]
	
	def byProgramNumber(programNumber: Short): Option[Entry] = table.find(e => e.programNumber == programNumber) 
	def byPid(pid: Short): Option[Entry]                     = table.find(e => e.pid == pid) 
	
	def getNitPid: Short = byProgramNumber(0) match {
	  case Some(entry) => entry.pid
	  case None        => 0x0010
	}
	
	def isPmtPid(pid: Short): Boolean = byPid(pid) match {
	  case Some(entry) => true
	  case None        => false
	}
	
	def getPmtPid(programNumber: Short): Integer = byProgramNumber(programNumber) match {
	  case Some(entry) => entry.pid
	  case None        => -1
	}
	
	def asObservable: Observable[Entry] = Observable.from(table)

    override def equals(other: Any) = other match {
	    case that: PsiPat => (that.table == this.table)
	    case _            => false
	}  
	
    private def generatePat(payload: WrappedArray[Byte]): WrappedArray[Entry] = {
	  generatePat(payload, new Array[Entry](0))
	}
	
	private def makeInt(b1: Byte, b2: Byte): Short = ((b1<<8) | b2).asInstanceOf[Short]
	
    private def generatePat(payload: WrappedArray[Byte], result: WrappedArray[Entry]): WrappedArray[Entry] = {
	  if(0 == payload.length) return result
	  val programNumber = makeInt(payload(0),payload(1))
	  if(-1 == programNumber) return result
	  val pid = makeInt(payload(2), payload(3))
	  generatePat(payload.slice(4,payload.length), result :+ new Entry(programNumber, pid))	    		    
	}
}

object PsiPat {
  def apply(packet: TsPacket) = new PsiPat {
    val table = generatePat(packet.payload)
  }
}