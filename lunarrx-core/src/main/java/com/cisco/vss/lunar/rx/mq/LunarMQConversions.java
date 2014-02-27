package com.cisco.vss.lunar.rx.mq;

import java.io.IOException;
import java.util.regex.Matcher;

import rx.Observable;
import rx.functions.Func1;

public class LunarMQConversions extends com.cisco.vss.rx.java.Conversions {
	
	public final static Converter<String, Matcher> parseMQUrl = parseString("MQ URL","([^:]+):([^/]+)/(.*)"); 
	
	public final static Converter<Matcher, LunarMQSocket> connectToServer = new Converter<Matcher, LunarMQSocket>() {
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
	
}
