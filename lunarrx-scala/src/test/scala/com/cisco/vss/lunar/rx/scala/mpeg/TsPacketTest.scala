package com.cisco.vss.lunar.rx.scala.mpeg

import org.scalatest.junit.JUnitSuite
import org.junit.Test
import org.junit.Assert._

class TsPacketTest extends JUnitSuite {
  @Test def correctPacket() {
    var buf = new Array[Byte](188)
    buf(0)  = 0x47
    buf(1)  = 0x61
    buf(2)  = 0xFD.toByte
    buf(3)  = 0x11
    for(i <- 4 until 188)
      buf(i) = i.toByte
    val packet = TsPacket(buf)
    
    assertFalse(packet.hasError)
    assertTrue(packet.hasPayloadStart)
    assertTrue(packet.hasPriority)
    assertEquals(0x1FD, packet.pid)
    assertTrue(packet.hasPayload)
    assertEquals(184, packet.payload.length)
    assertEquals(1, packet.continuityCounter)
    assertEquals(187.toByte, packet.payload(183))
  }

}