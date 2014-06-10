package com.cisco.vss.lunar.rx.scala.plugin

import org.scalatest.junit.JUnitSuite
import org.junit.Test
import org.junit.Assert._
import java.io._

class IoOpsTest extends JUnitSuite {
  @Test def testWaitForPattern() {
	  val buf   = Array[Byte](0x12, 0x49, 0x45, 0x4E, 0x4F,0x49, 0x45, 0x4E, 0x44, 0x25, 0x26)                                                  
	  val input = new ByteArrayInputStream(buf)       
	  val reader= new BufferedInputStream(input)       
	  val crc = new Array[Byte](2)                    
	  val l = waitForPattern(reader, List(0x49, 0x45, 0x4E, 0x44)).read(crc)
	  
	  assert(Array[Byte](0x25,0x26) === crc)
  }
  
  @Test def testReadText() {
    val text   = "Line 1\nLine2\n"
    val input  = new ByteArrayInputStream(text.getBytes())
    
    assert(text === readText(input))
  }
}