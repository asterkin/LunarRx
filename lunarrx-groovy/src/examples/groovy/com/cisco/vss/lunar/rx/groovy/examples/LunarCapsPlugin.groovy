package com.cisco.vss.lunar.rx.groovy.examples
import com.cisco.vss.lunar.rx.plugin.core.Lunar
import com.cisco.vss.lunar.rx.plugin.schema.subtitletext.*
import com.cisco.vss.lunar.rx.plugin.schema.capsrx.*
import rx.Observable

class LunarCapsPlugin {
	static buildCaps = { input ->
		return input
				.map({sub   -> sub.getText()})
				.map({text  -> text.split("\\s+")})
				.map({words -> words.collect({word -> word.trim()})
								    .findAll({word -> !word.isEmpty()})
								    .findAll({word -> Character.isUpperCase(word[0] as Character)})})
				.map({caps  -> new Caps(caps as String[])})
	}

	static main(args) {
		final lunar  = new Lunar(args)
		
		lunar.transform(Subtitles.class, buildCaps, Caps.class)
	}
}
