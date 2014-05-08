package com.cisco.vss.lunar.rx.plugin.scala

import org.scalatest.junit.JUnitSuite
import org.junit.Test
import org.junit.Assert._
import rx.lang.scala._
import com.cisco.vss.lunar.rx.plugin.scala.ObservableEx._

class ObservableExTest extends JUnitSuite {
	@Test def testReduceUntil() {
	  val input = Observable.from(Array("aa","bbb","ccc\n","dd\n"))
	  
	  val result = input.reduceUntil("", (s1,s2) => s1+s2, (s) => '\n'==s.last).toBlockingObservable.toIterable
	  
	  assert(Array("aabbbccc\n","dd\n").sameElements(result))
	}
}