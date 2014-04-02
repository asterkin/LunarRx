package com.cisco.vss.lunar.rx.plugin.core;

import java.net.MalformedURLException;
import java.net.URL;
import rx.Observable;
import static com.cisco.vss.lunar.rx.plugin.core.LunarConversions.*;
import static com.cisco.vss.lunar.rx.plugin.core.TrackStatus.*;

public class Lunar {
	private final String hostName;
	private final int    port;
	
	public Lunar(final String hostName, final int port) {
		this.hostName    = hostName;
		this.port        = port;
	}
	
	<R, T extends LunarDataResponse<R[]>> Observable<R> getArrayResponse(final String path, final Class<T> responseType, final Class<R> dataType) {
		final URL  url = makeUrl(path);
		
		return Observable.from(url)
				.flatMap(synchHttpGet)
				.flatMap(jsonString2Object(responseType))
				.flatMap(getArrayData(dataType))
				.flatMap(flatten(dataType));
	}

	private URL makeUrl(final String path) {
		try {
			return new URL("http",hostName,port, path);
		} catch (MalformedURLException e) {
			//Should not get there
			e.printStackTrace();
		}
		return null;
	}

	<R, T extends LunarDataResponse<R[]>> Observable<LunarNotify<R>> getNotifyArrayResponse(final String category, final Class<T> responseType, final Class<R> dataType) {
		final String path = String.format("/%s", category);
		return getArrayResponse(path, responseType, dataType)
			   .map(notifyAdd(dataType));
	}
	
	Observable<String> getUpdatesUrl(final String category) {
		final String path = String.format("/updates/%s", category);
		final URL  url = makeUrl(path);
		
		return Observable.from(url)
				.flatMap(synchHttpGet)
				.flatMap(jsonString2Object(LunarUrlData.Response.class))
				.flatMap(getUrlData);
	}
	
	<R, T extends LunarStatusUpdateMessage<R>> Observable<LunarNotify<R>> getStatusUpdatesStream(final String category, final Class<T> messageType, final Class<R> dataType) {
		return getUpdatesUrl(category)
				.flatMap(parseMQUrl)
				.flatMap(connectToServer)
				.flatMap(readStream)
				.map(byte2String)
				.flatMap(jsonString2Object(messageType))
				.flatMap(statusUpdate2Notify(messageType,dataType));
	}
	
	<R extends LunarEntity, T extends LunarStatusUpdateMessage<R>, S extends LunarDataResponse<R[]>> Observable<LunarNotify<R>> getCombinedNotifyStream(final String category, final Class<T> messageType, final Class<S> responseType, final Class<R> dataType) {
		final Observable<LunarNotify<R>> updates = getStatusUpdatesStream(category, messageType, dataType);
		final Observable<LunarNotify<R>> current = getNotifyArrayResponse(category, responseType, dataType);
		//TODO to optimize such that it happens only during the fetch of initial table
		return Observable.merge(updates, current)
			   .filter(prematureRemove(dataType)); //filter OUT premature removes if happen
	}
	
	public Observable<LunarNotify<LunarSource>> getSources() {
		return getCombinedNotifyStream("sources", LunarSource.StatusUpdateMessage.class, LunarSource.Response.class, LunarSource.class);
	}

	public Observable<LunarNotify<LunarTrack>> getTracks() {
		return getCombinedNotifyStream("tracks", LunarTrack.StatusUpdateMessage.class, LunarTrack.Response.class, LunarTrack.class);
	}

	Observable<LunarMQWriter> getOutputTrackStream(final String developerID, final LunarTrack track) {
		final URL url = makeUrl(track.streamerRequestPath(developerID));
		return Observable.from(url)
			   .flatMap(synchHttpGet)
			   .flatMap(jsonString2Object(TrackInfoResponse.class))
			   .flatMap(getResultData)
			   .map(getURL)
			   .flatMap(parseMQUrl)
			   .flatMap(connectToServer)
			   .map(createRawWriter);
	}
		
	Observable<LunarResponse> sendReport(final LunarPluginStateReport report) {
		final String json = object2JsonString(LunarPluginStateReport.class).call(report);
		final URL    url  = makeUrl("/state/plugins");
		return Observable.from(url)
				.flatMap(synchHttpPost(json))
				.flatMap(jsonString2Object(LunarResponse.class))
				.flatMap(checkResult(LunarResponse.class));
	}

	//So far new Application API
	public Observable<TracksStatusUpdate> getTracksStatusUpdateStream() {
		return getUpdatesUrl("tracks")
				.flatMap(parseMQUrl)
				.flatMap(connectToServer)
				.flatMap(readStream)
				.map(byte2String)
				.flatMap(jsonString2Object(TracksStatusUpdate.class));		
	}

	public Observable<LunarTrack> getTrackInfoFromUpdate(final Integer sourceID, final String pluginName, final String trackName) {
		return getTracksStatusUpdateStream()
			   .filter(checkStatus(TRACK_IS_UP))
			   .flatMap(getTracks)
			   .filter(findTrack(new LunarTrack(sourceID,pluginName,trackName)));
	}

	public Observable<LunarTrack> getTrackInfoFromRest(final Integer sourceID, final String pluginName, final String trackName) {
		final LunarTrack template = new LunarTrack(sourceID,pluginName,trackName);
		final URL       url       = makeUrl(template.httpGetRequestPath());
		return Observable.from(url)
				.flatMap(synchHttpGet)
				.flatMap(jsonString2Object(TrackInfoResponse.class))
				.flatMap(getResultData);		
	}
	
	public Observable<byte[]> getInputTrackStream(final Integer sourceID, final String pluginName, final String trackName) {
//		return Observable.amb(getTrackInfoFromUpdate(sourceID,pluginName,trackName),
				return getTrackInfoFromRest(sourceID,pluginName,trackName)
				.map(getURL)
				.flatMap(parseMQUrl)
				.flatMap(connectToServer)
				.flatMap(readStream);
	}
	
	public <T> Observable<T> getInputTrackItemStream(final Class<T> clazz, final Integer sourceID, final String pluginName, final String trackName) {
		return getInputTrackStream(sourceID, pluginName, trackName)
		   .map(byte2String)
		   .flatMap(jsonString2Object(clazz));
	}
	
}
