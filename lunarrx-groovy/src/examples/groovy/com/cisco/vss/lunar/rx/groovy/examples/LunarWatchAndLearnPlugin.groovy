package com.cisco.vss.lunar.rx.groovy.examples
import com.cisco.vss.lunar.rx.plugin.core.Lunar
import com.cisco.vss.lunar.rx.plugin.schema.*
import com.cisco.vss.lunar.rx.plugin.schema.subtitletext.Subtitles;

import static com.cisco.vss.rx.java.Conversions.*
import rx.Observable

class LunarWatchAndLearnPlugin {
	static main(args) {
		final HOST         = args[0]
		final PORT         = Integer.parseInt(args[1])
		final lunar        = new Lunar(HOST, PORT)
		final SOURCE_ID    = 1
		final INPUT_PLUGIN = "subtitletext"
		final INPUT_TRACK  = "subtitles"
		final ts           = lunar.getInputTrackItemStream(Subtitles.class, SOURCE_ID, INPUT_PLUGIN, INPUT_TRACK)
		
		ts
		.map({Subtitles sub -> return sub.getText()})
		.map({String text -> return text.split("[ .,?!']")})
		.flatMap({String[] wordList -> Observable.from(wordList)})
		.map({String word -> word.trim()})
		.filter({String word -> return word ==~ /\w\w\w+/})
		.map({String word -> new URL("http","www.wordcount.org",String.format("/dbquery.php?toFind=%s&method=SEARCH_BY_NAME", word))})
		.flatMap(synchHttpGet)
		.map({String result -> return result.split("&")})
	    .filter({String[] result -> return result[1] == "wordFound=yes"})
		.map({String[] result -> [result[4].split("=")[1], Integer.parseInt(result[3].split("=")[1])]})
		.subscribe(
			{tuple -> println(tuple)}
			,{Throwable err -> println(err)}//TODO: better diagnostics?
		   ,{ -> println("Unexpected EOF")}
		)
	}
}
