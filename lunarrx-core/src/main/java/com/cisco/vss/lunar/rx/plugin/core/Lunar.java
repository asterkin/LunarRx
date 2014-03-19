package com.cisco.vss.lunar.rx.plugin.core;

import java.net.MalformedURLException;
import java.net.URL;

import rx.Observable;
import rx.functions.Func1;
import static com.cisco.vss.lunar.rx.mq.LunarMQConversions.*;
import static com.cisco.vss.lunar.rx.plugin.core.LunarResponseResult.*;

public class Lunar {
	private final String hostName;
	private final int    port;
	private final String developerID;
	
	public Lunar(final String hostName, final int port, final String developerID) {
		this.hostName    = hostName;
		this.port        = port;
		this.developerID = developerID;
	}
	
	private static final Converter<TrackInfoResponse, TrackInfo> getResultData = new Converter<TrackInfoResponse, TrackInfo>() {
		@Override
		protected TrackInfo convert(final TrackInfoResponse message)	throws Throwable {
			if(OK != message.result) throw new Exception("Lunar Response is NOT OK");
			return message.data[0]; //TODO: more generic?
		}
	};

	private static final Func1<TrackInfo, String> getURL = new Func1<TrackInfo, String>() {
		@Override
		public String call(final TrackInfo info) {
			return info.url;
		}
	};

	private static final Converter<UpdatesTracksResponse, UpdatesTracksResponse.Data> getResultData1 = new Converter<UpdatesTracksResponse, UpdatesTracksResponse.Data>() {
		@Override
		protected UpdatesTracksResponse.Data convert(final UpdatesTracksResponse message)	throws Throwable {
			if(OK != message.result) throw new Exception("Lunar Response is NOT OK");
			return message.data; //TODO: more generic?
		}
	};
	

	private static final Func1<UpdatesTracksResponse.Data, String> getURL1 = new Func1<UpdatesTracksResponse.Data, String>() {
		@Override
		public String call(final UpdatesTracksResponse.Data data) {
			return data.url;
		}
	};
	
	public Observable<TracksStatusUpdate> getTracksStatusUpdateStream() throws MalformedURLException {
		final URL url = new URL("http",hostName,port,"/updates/tracks");
		return Observable.from(url)
				.flatMap(synchHttpGet)
				.flatMap(jsonString2Object(UpdatesTracksResponse.class))
				.flatMap(getResultData1)
				.map(getURL1)
				.flatMap(parseMQUrl)
				.flatMap(connectToServer)
				.flatMap(readStream)
				.map(byte2String)
				.flatMap(jsonString2Object(TracksStatusUpdate.class));		
	}
	
	public Observable<byte[]> getInputTrackStream(final Integer sourceID, final String pluginName, final String trackName) throws MalformedURLException {
		final TrackInfo template = new TrackInfo(sourceID,pluginName,trackName);
		final URL       url      = new URL("http",hostName,port,template.httpGetRequestPath());
		return Observable.from(url)
				.flatMap(synchHttpGet)
				.flatMap(jsonString2Object(TrackInfoResponse.class))
				.flatMap(getResultData)
				.map(getURL)
				.flatMap(parseMQUrl)
				.flatMap(connectToServer)
				.flatMap(readStream);
	}
	
	public <T> Observable<T> getInputTrackItemStream(final Class<T> clazz, final Integer sourceID, final String pluginName, final String trackName) throws MalformedURLException {
		return getInputTrackStream(sourceID, pluginName, trackName)
		   .map(byte2String)
		   .flatMap(jsonString2Object(clazz));
	}

	public LunarMQWriter getOutputTrackStream(final String sourceID, final String pluginName, final String trackName) {
		return null;
	}
	
	public <T extends TrackItem> LunarTractItemWriter<T> getOutputTractItemStream(final Class<T> clazz, final String sourceID, final String pluginName, final String trackName) {
		return null;
	}
}
