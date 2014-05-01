package com.cisco.vss.lunar.rx.java8.examples;

import com.cisco.vss.lunar.rx.plugin.core.Lunar;
import com.cisco.vss.lunar.rx.plugin.schema.capsrx.*;
import com.cisco.vss.lunar.rx.plugin.schema.subtitletext.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import rx.Observable;

class LunarCapsPlugin {
        final static String[] extractCaps(final List<String> words) {
            final List<String> caps = words.stream()
                    .map(word -> word.trim())
                    .filter(word -> !word.isEmpty())
                    .filter(word -> Character.isUpperCase(word.charAt(0)))
                    .collect(Collectors.toList());
            return caps.toArray(new String[]{});
        }
        
	final static Observable<Caps> buildCaps(final Observable<Subtitles> input) {
		return input
                        .map(sub  -> sub.getText())
                        .map(text -> Arrays.asList(text.split("\\s+")))
                        .map(words-> extractCaps(words))
                        .map(caps -> new Caps(caps));
	}
        
        final static String[] getArgs(final String argv[]) {
            return argv.length>0 ? argv : new String[] {
                    "6871c4b35301671668ebf26ae46b6441", 
                    "caprsrx", 
                    "54.72.89.87", 
                    "7000"
            };

        }
        public final static void main(final String[] args) {
            new Lunar(getArgs(args)).transform(Subtitles.class, LunarCapsPlugin::buildCaps, Caps.class);
        }
}