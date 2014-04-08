package com.cisco.vss.lunar.rx.scala.examples

import com.cisco.vss.lunar.rx.plugin.scala.Lunar
import com.cisco.vss.lunar.rx.plugin.schema.SubtitlesTrackItem
import rx.lang.scala.Observable
import java.net.URL

object LunarWatchAndLearnPlugin {
    //TODO: move to Lunar?	
	implicit class Regex(sc: StringContext) {
	  def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
	}
	
	def main(args: Array[String]): Unit = {
	    val HOST         = args(0)
	    val PORT         = Integer.parseInt(args(1))
	    val DEVELOPER_ID = "6871c4b35301671668ebf26ae46b6441";
	    val lunar        = Lunar(HOST, PORT, DEVELOPER_ID)
	    val SOURCE_ID    = 1
	    val INPUT_PLUGIN = "subtitletext"
	    val INPUT_TRACK  = "subtitles"
	    val ts           = lunar.getInputTrackItemStream(classOf[SubtitlesTrackItem], SOURCE_ID, INPUT_PLUGIN, INPUT_TRACK)
	    
	    ts
	    .map(sub => sub.getText())
	    .map(text => text.split("[ .,?!']"))
	    .flatMap(wordList => Observable.from(wordList))
	    .map(word => word.trim())
	    .filter(word => word match { case r"\w\w\w+" => true case _ => false})
	    .map(word => new URL("http","www.wordcount.org",String.format("/dbquery.php?toFind=%s&method=SEARCH_BY_NAME", word)))
	    .flatMap(url => lunar.synchHttpGet(url))
	    .map(result => result.split("&"))
	    .filter(result => result(1) == "wordFound=yes")
	    .map(result => (result(4).split("=")(1), Integer.parseInt(result(3).split("=")(1))))
	    .subscribe(
	        result  => {
	          println(result)
	        }
	        ,err => {
	          println("Error!",err)
	        }
	       ,() => println("Unexpected EOF")        
	    )    
	}
}