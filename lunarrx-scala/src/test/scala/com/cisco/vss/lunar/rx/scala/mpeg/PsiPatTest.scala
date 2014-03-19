package com.cisco.vss.lunar.rx.scala.mpeg

import org.scalatest.junit.JUnitSuite
import org.junit.Test
import org.junit.Assert._

class PsiPatTest extends JUnitSuite {
  @Test def correctPat() {
    val buf = Array[Byte](
        0x47,       //sync
        0x60, 0x00, //pid
        0x01,       //counter
        0x00,0x00, /*->*/ 0xAA.toByte, 0xBB.toByte,
        0x00,0x01, /*->*/ 0xCC.toByte, 0xDD.toByte,
        0x01,0x00, /*->*/ 0xEE.toByte, 0xFF.toByte,
        0xFF.toByte,0xFF.toByte, /*->*/ 0x00, 0x00
    )
    val packet = TsPacket(buf)
    val pat    = PsiPat(packet)
    
    assertEquals(0xAABB, pat.getNitPid)
  }

}