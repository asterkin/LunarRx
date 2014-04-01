package com.cisco.vss.lunar.rx.plugin.core;

import java.net.MalformedURLException;
import java.net.URL;

import rx.Observable;
import rx.functions.Func1;
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
	
	<R, T extends LunarResponse<R[]>> Observable<R> getArrayResponse(final String path, final Class<T> responseType, final Class<R> dataType) throws MalformedURLException {
		final URL  url = new URL("http",hostName,port, path);
		
		return Observable.from(url)
				.flatMap(synchHttpGet)
				.flatMap(jsonString2Object(responseType))
				.flatMap(getArrayData(dataType))
				.flatMap(flatten(dataType));
	}

	<R, T extends LunarResponse<R[]>> Observable<LunarNotify<R>> getNotifyArrayResponse(final String category, final Class<T> responseType, final Class<R> dataType) throws MalformedURLException {
		final String path = String.format("/%s", category);
		return getArrayResponse(path, responseType, dataType)
			   .map(notifyAdd(dataType));
	}
	
	Observable<String> getUpdatesUrl(final String category) throws MalformedURLException {
		final String path = String.format("/updates/%s", category);
		final URL  url = new URL("http",hostName,port, path);
		
		return Observable.from(url)
				.flatMap(synchHttpGet)
				.flatMap(jsonString2Object(LunarUrlData.Response.class))
				.flatMap(getUrlData);
	}
	
	<R, T extends LunarStatusUpdateMessage<R>> Observable<LunarNotify<R>> getStatusUpdatesStream(final String category, final Class<T> messageType, final Class<R> dataType) throws MalformedURLException {
		return getUpdatesUrl(category)
				.flatMap(parseMQUrl)
				.flatMap(connectToServer)
				.flatMap(readStream)
				.map(byte2String)
				.flatMap(jsonString2Object(messageType))
				.flatMap(statusUpdate2Notify(messageType,dataType));
	}
	
	<R extends LunarEntity, T extends LunarStatusUpdateMessage<R>, S extends LunarResponse<R[]>> Observable<LunarNotify<R>> getCombinedNotifyStream(final String category, final Class<T> messageType, final Class<S> responseType, final Class<R> dataType) throws MalformedURLException {
		final Observable<LunarNotify<R>> updates = getStatusUpdatesStream(category, messageType, dataType);
		final Observable<LunarNotify<R>> current = getNotifyArrayResponse(category, responseType, dataType);
		//TODO to optimize such that it happens only during the fetch of initial table
		return Observable.merge(updates, current)
			   .filter(prematureRemove(dataType)); //filter OUT premature removes if happen
	}
	
	public Observable<LunarNotify<LunarSource>> getSources() throws MalformedURLException {
		return getCombinedNotifyStream("sources", LunarSource.StatusUpdateMessage.class, LunarSource.Response.class, LunarSource.class);
	}

	public Observable<LunarNotify<LunarTrack>> getTracks() throws MalformedURLException {
		return getCombinedNotifyStream("tracks", LunarTrack.StatusUpdateMessage.class, LunarTrack.Response.class, LunarTrack.class);
	}

	Observable<LunarMQWriter> getOutputTrackStream(final LunarTrack track) throws MalformedURLException {
		final URL url = new URL("http",hostName,port,track.streamerRequestPath(developerID));
		//TODO: embed reporting capabilities somewhere here
		return Observable.from(url)
			   .flatMap(synchHttpGet)
			   .flatMap(jsonString2Object(TrackInfoResponse.class))
			   .flatMap(getResultData)
			   .map(getURL)
			   .flatMap(parseMQUrl)
			   .flatMap(connectToServer)
			   .map(createRawWriter);
	}
	
	public <T extends TrackItem, R extends TrackItem> LunarTrackFilter<T,R> getFilter(final Func1<T, Observable<R>> transform) throws MalformedURLException {
		final Observable<LunarNotify<LunarTrack>> input  = getTracks().filter(pluginTrack(null)); //TODO: where to get a prototype from?
		final Observable<LunarMQWriter>           output = getOutputTrackStream(null); //TODO: where to get a prototype from?
		return new LunarTrackItemFilter<T,R>(input, transform, output);
	}
	
	//So far new Application API
	public Observable<TracksStatusUpdate> getTracksStatusUpdateStream() throws MalformedURLException {
		return getUpdatesUrl("tracks")
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
	
}
