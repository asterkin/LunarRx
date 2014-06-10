package com.cisco.vss.lunar.rx.scala.plugin

import org.scalatest.junit.JUnitSuite
import org.junit.Test
import org.junit.Assert._
import rx.lang.scala._

class ObservableOpsTest extends JUnitSuite {
	@Test def testReduceUntil() {
	  val input = Observable.from(Array("aa","bbb","ccc\n","dd\n"))
	  
	  val result = input.reduceUntil("", (s1:String, s2) => s1+s2, (s) => '\n'==s.last).toBlockingObservable.toIterable
	  
	  assert(Array("aabbbccc\n","dd\n").sameElements(result))
	}
}