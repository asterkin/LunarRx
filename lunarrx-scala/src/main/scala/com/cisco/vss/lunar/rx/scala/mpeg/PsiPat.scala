package com.cisco.vss.lunar.rx.scala.mpeg
import rx.lang.scala._
import scala.collection.mutable.WrappedArray

abstract class PsiPat {
	sealed class Entry(val programNumber: Integer, val pid: Integer) {
    override def equals(other: Any) = other match {
	    case that: PsiPat#Entry => (that.programNumber == this.programNumber) && (that.pid == this.pid)
	    case _                  => false
	}  
	  
	}
	val table: WrappedArray[Entry]
	
	def byProgramNumber(programNumber: Integer): Option[Entry] = table.find(e => e.programNumber == programNumber) 
	def byPid(pid: Integer): Option[Entry]                     = table.find(e => e.pid == pid) 
	
	def getNitPid: Integer = byProgramNumber(0) match {
	  case Some(entry) => entry.pid
	  case None        => 0x0010
	}
	
	def isPmtPid(pid: Integer): Boolean = byPid(pid) match {
	  case Some(entry) => true
	  case None        => false
	}
	
	def getPmtPid(programNumber: Integer): Integer = byProgramNumber(programNumber) match {
	  case Some(entry) => entry.pid
	  case None        => 0xFFFF
	}
	
	def asObservable: Observable[Entry] = Observable.from(table)

    override def equals(other: Any) = other match {
	    case that: PsiPat => (that.table == this.table)
	    case _            => false
	}  
	
    private def generatePat(payload: WrappedArray[Byte]): WrappedArray[Entry] = {
	  generatePat(payload, new Array[Entry](0))
	}
	
    private def generatePat(payload: WrappedArray[Byte], result: WrappedArray[Entry]): WrappedArray[Entry] = {
	  if(0 == payload.length) return result
	  val programNumber = UnsignedShort(payload(0),payload(1))
	  if(0xFFFF == programNumber) return result
	  val pid = UnsignedShort(payload(2), payload(3))
	  generatePat(payload.slice(4,payload.length), result :+ new Entry(programNumber, pid))	    		    
	}
}

object PsiPat {
  def apply(packet: TsPacket) = new PsiPat {
    val table = generatePat(packet.payload)
  }
}