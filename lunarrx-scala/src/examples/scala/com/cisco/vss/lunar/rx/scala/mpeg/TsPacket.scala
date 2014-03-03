package com.cisco.vss.lunar.rx.scala.mpeg

abstract class TsPacket {
  private [scala] val packet : Array[Byte]
  
  def pid : Integer = {
    (packet(1)&0x1F << 8) | packet(2)
  }
  
  def payload : Array[Byte] = {
    val offset = payloadOffset
    packet.slice(offset, packet.length)
  }
  
  private def payloadOffset: Integer = {
    if (adaptationFieldExist) 4+packet(4) else 4
  }
  
  private def adaptationFieldExist: Boolean = {
    0 != (packet(3) & 0x20)
  }
}

object TsPacket {
  def apply(buf: Array[Byte]): TsPacket = new TsPacket {
    val packet = buf
  }
}