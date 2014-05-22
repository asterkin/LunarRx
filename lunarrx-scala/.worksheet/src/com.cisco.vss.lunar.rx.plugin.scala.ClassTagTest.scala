package com.cisco.vss.lunar.rx.plugin.scala
import com.cisco.vss.lunar.rx.plugin.core.{LunarTrack, LunarTrackItem}
import scala.reflect._

class MyClass(val l: Int) extends LunarTrackItem {}

object ClassTagTest {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(281); 
  def transform[R](clazz: Class[R]): Unit = {
  	println(clazz)
  };System.out.println("""transform: [R](clazz: Class[R])Unit""");$skip(156); 
  
	def paramInfo[T <: LunarTrackItem : ClassTag](func: Array[Byte] => T): Unit = {
	  val ct = classTag[T].runtimeClass
	  println(ct)
	  transform(ct)
	};System.out.println("""paramInfo: [T <: com.cisco.vss.lunar.rx.plugin.core.LunarTrackItem](func: Array[Byte] => T)(implicit evidence$2: scala.reflect.ClassTag[T])Unit""");$skip(46); 
	
	paramInfo(buf => new  MyClass(buf.length))}
}
