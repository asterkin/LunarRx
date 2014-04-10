package com.cisco.vss.lunar.rx.mq;

import java.io.IOException;
import java.util.regex.Matcher;

import rx.Observable;
import rx.functions.Func1;

public class LunarMQConversions extends com.cisco.vss.rx.java.Conversions {
	
	public final static Converter<String, Matcher> parseMQUrl = parseString("MQ URL","([^:]+):([^/]+)/(.*)"); 
	
	public final static Converter<Matcher, LunarMQSocket> connectToServer = new Converter<Matcher, LunarMQSocket>("connectToServer") {
		@Override
		protected LunarMQSocket convert(final Matcher matcher) throws IOException, LunarMQException {
			return LunarMQSocket.createSocket(
					matcher.group(1), 
					Integer.parseInt(matcher.group(2)), 
					matcher.group(3));
		}
	};

	public final static Func1<LunarMQSocket, Observable<byte[]>> readStream = new Func1<LunarMQSocket, Observable<byte[]>>() {
		@Override
		public Observable<byte[]> call(final LunarMQSocket socket){
			return Observable.create(new LunarMQOnSubscribe(socket));
		}		
	};
	
	public static Observable<byte[]> getMQStream(final Observable<String> urlSource) {
		return urlSource
				.flatMap(parseMQUrl)
				.flatMap(connectToServer)
				.flatMap(readStream);
	}
	
	public static Func1<String, String> normalizeJson = new Func1<String, String>() {//Temporal hack
		@Override
		public String call(final String message) {
			return message.replace("pluginName", "plugin").replace("trackName", "track");
		}		
	};
	
	public static <R> Observable<R> getMQStream(final Observable<String> urlSource, final Class<R> messageType) {
		return getMQStream(urlSource)
				.map(byte2String)
				.map(normalizeJson)
				.flatMap(jsonString2Object(messageType));
	}
}
