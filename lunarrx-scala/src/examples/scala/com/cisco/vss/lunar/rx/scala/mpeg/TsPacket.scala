package com.cisco.vss.lunar.rx.scala.mpeg
import scala.collection.mutable.WrappedArray

abstract class TsPacket {
  private [scala] val packet : WrappedArray[Byte]
  
  def hasError: Boolean        = (0 != (packet(1) & 0x80))
  def hasPayloadStart: Boolean = (0 != (packet(1) & 0x40))
  def hasPriority: Boolean     = (0 != (packet(1) & 0x20))
  
  def pid : Integer = ((packet(1)&0x1F) << 8) | packet(2)
  
  def payload : WrappedArray[Byte] = {
    val offset = payloadOffset
    packet.slice(offset, packet.length)
  }
  
  private def payloadOffset: Integer = {
    if (adaptationFieldExist) 4+packet(4) else 4
  }
  
  private def adaptationFieldExist: Boolean = {
    0 != (packet(3) & 0x20)
  }
  
  override def equals(other: Any) = other match {
    case that: TsPacket => that.packet == this.packet
    case _              => false
  }  
}

object TsPacket {
  def apply(buf: WrappedArray[Byte]): TsPacket = new TsPacket {
    val packet = buf
  }
}