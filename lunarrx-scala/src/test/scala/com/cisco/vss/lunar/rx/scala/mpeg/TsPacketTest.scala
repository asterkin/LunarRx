package com.cisco.vss.lunar.rx.scala.mpeg

import org.scalatest.junit.JUnitSuite
import org.junit.Test
import org.junit.Assert._

class TsPacketTest extends JUnitSuite {
  @Test def correctPacket() {
    var buf = new Array[Byte](188)
    buf(0)  = 0x47
    buf(1)  = 0x60
    buf(2)  = 0x0D
    buf(3)  = 0x01
    for(i <- 4 until 188)
      buf(i) = i.toByte
    val packet = TsPacket(buf)
    
    assertFalse(packet.hasError)
    assertTrue(packet.hasPayloadStart)
    assertTrue(packet.hasPriority)
    assertEquals(13, packet.pid)
    assertEquals(184, packet.payload.length)
    assertEquals(187.toByte, packet.payload(183))
  }

}