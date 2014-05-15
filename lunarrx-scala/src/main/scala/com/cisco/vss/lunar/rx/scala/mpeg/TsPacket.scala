package com.cisco.vss.lunar.rx.scala.mpeg
import scala.collection.mutable.WrappedArray

object UnsignedByte {
  def apply(b: Byte): Integer = (0xFF & b)
}

object UnsignedShort {
  def apply(b1: Byte, b2: Byte): Integer = (UnsignedByte(b1)*256 + UnsignedByte(b2))
}

abstract class TsPacket {
  private [scala] val packet : WrappedArray[Byte]
  
  def hasError: Boolean           = (0 != (UnsignedByte(packet(1)) & 0x80))
  def hasPayloadStart: Boolean    = (0 != (UnsignedByte(packet(1)) & 0x40))
  def hasPriority: Boolean        = (0 != (UnsignedByte(packet(1)) & 0x20))
  def pid : Integer               = 0x1FFF & UnsignedShort(packet(1), packet(2))
  def hasAdaptationField: Boolean = (0 != (UnsignedByte(packet(3)) & 0x20))
  def hasPayload: Boolean         = (0 != (UnsignedByte(packet(3)) & 0x10))
  def continuityCounter: Integer  = UnsignedByte(packet(3)) & 0x0F
  
  def payload : WrappedArray[Byte] = {
    val offset = payloadOffset
    packet.slice(offset, packet.length)
  }
  
  private def payloadOffset: Integer = {
    if (hasAdaptationField) 4 + UnsignedByte(packet(4)) else 4
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