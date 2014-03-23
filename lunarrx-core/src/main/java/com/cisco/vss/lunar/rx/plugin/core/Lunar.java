package com.cisco.vss.lunar.rx.plugin.core;

import java.net.MalformedURLException;
import java.net.URL;
import rx.Observable;
import static com.cisco.vss.lunar.rx.plugin.core.LunarConversions.*;
import static com.cisco.vss.lunar.rx.plugin.core.TrackStatus.*;

public class Lunar {
	private final String hostName;
	private final int    port;
	private final String developerID;
	
	public Lunar(final String hostName, final int port, final String developerID) {
		this.hostName    = hostName;
		this.port        = port;
		this.developerID = developerID;
	}
	
	private <R, T extends LunarResponse<R>> Observable<R> getResponse(final String path, final Class<T> responseType, final Class<R> dataType) throws MalformedURLException {
		final URL  url = new URL("http",hostName,port, path);
		
		return Observable.from(url)
				.flatMap(synchHttpGet)
				.flatMap(jsonString2Object(responseType))
				.flatMap(getResultData(dataType))
				.flatMap(flatten(dataType));	
	}
	
	public Observable<LunarSource> getSources() throws MalformedURLException {
		return getResponse("/sources", LunarSource.Response.class, LunarSource.class);
	}
	
	public Observable<String> getUpdatesUrl(final String category) throws MalformedURLException {
		final String path = String.format("/updates/%s", category);
		return getResponse(path,LunarUrlData.Response.class, LunarUrlData.class)
			   .map(getUrl);
	}
	
	public Observable<LunarTrack> getTracks(final String pluginName, final String trackName) throws MalformedURLException {
		final String path = String.format("/tracks?pluginName=%s&trackName=%s", pluginName, trackName);
		return getResponse(path, LunarTrack.Response.class, LunarTrack.class);
	}
	
	public Observable<LunarTrack> getTracks(final int sourceID) throws MalformedURLException {
		final String path = String.format("/tracks?sourceID=%d", sourceID);
		return getResponse(path, LunarTrack.Response.class, LunarTrack.class);
	}
	
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

	public Observable<LunarTrack> getTrackInfoFromUpdate(final Integer sourceID, final String pluginName, final String trackName) throws MalformedURLException {
		return getTracksStatusUpdateStream()
			   .filter(checkStatus(TRACK_IS_UP))
			   .flatMap(getTracks)
			   .filter(findTrack(new LunarTrack(sourceID,pluginName,trackName)));
	}

	public Observable<LunarTrack> getTrackInfoFromRest(final Integer sourceID, final String pluginName, final String trackName) throws MalformedURLException {
		final LunarTrack template = new LunarTrack(sourceID,pluginName,trackName);
		final URL       url      = new URL("http",hostName,port,template.httpGetRequestPath());
		return Observable.from(url)
				.flatMap(synchHttpGet)
				.flatMap(jsonString2Object(TrackInfoResponse.class))
				.flatMap(getResultData);		
	}
	
	public Observable<byte[]> getInputTrackStream(final Integer sourceID, final String pluginName, final String trackName) throws MalformedURLException {
//		return Observable.amb(getTrackInfoFromUpdate(sourceID,pluginName,trackName),
				return getTrackInfoFromRest(sourceID,pluginName,trackName)
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

	public Observable<LunarMQWriter> getOutputTrackStream(final LunarTrack track) throws MalformedURLException {
		final URL url = new URL("http",hostName,port,track.streamerRequestPath(developerID));
		return Observable.from(url)
			   .flatMap(synchHttpGet)
			   .flatMap(jsonString2Object(TrackInfoResponse.class))
			   .flatMap(getResultData)
			   .map(getURL)
			   .flatMap(parseMQUrl)
			   .flatMap(connectToServer)
			   .map(createRawWriter);
	}
	
	public <T extends TrackItem> LunarTractItemWriter<T> getOutputTractItemStream(final Class<T> clazz, final String sourceID, final String pluginName, final String trackName) {
		return null;
	}
}
