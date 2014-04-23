package com.cisco.vss.lunar.rx.plugin.core;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cisco.vss.lunar.rx.mq.LunarMQWriter;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import static com.cisco.vss.lunar.rx.mq.LunarMQConversions.getMQStream;
import static com.cisco.vss.lunar.rx.plugin.core.LunarConversions.*;
import static com.cisco.vss.lunar.rx.plugin.core.LunarTrackTemplateFactory.*;

public class Lunar {
	public Lunar(final String[] args) {
	    //   HOST     PORT                       DEVELOPER_ID
		this(args[2] ,Integer.parseInt(args[3]), args[0]);
	}

	public Observable<LunarNotify<LunarSource>> getSources() {
		return getCombinedNotifyStream("sources", LunarSource.StatusUpdateMessage.class, LunarSource.Response.class, LunarSource.class);
	}

	public Observable<LunarNotify<LunarTrack>> getTracks() {
		return getCombinedNotifyStream("tracks", LunarTrack.StatusUpdateMessage.class, LunarTrack.Response.class, LunarTrack.class);
	}

	public Observable<LunarNotify<LunarTrack>> getTracks(final LunarTrack template) {
		return getTracks().filter(pluginTrack(template));
	}
	
	public void transform(final LunarTrack sourceTemplate, final Func2<Observable<byte[]>, LunarTrack, Observable<? extends byte[]>> trans, final LunarTrack resultTemplate) {
		final LunarByteStreamTransformer transformer = new LunarByteStreamTransformer(this, sourceTemplate, trans, resultTemplate);
		transformer.run();
	}

	public <R extends LunarTrackItem> void transform(final LunarTrack sourceTemplate, final Func1<Observable<byte[]>, Observable<? extends R>> trans, final Class<R> resultType) {
		final LunarTrack                       resultTemplate = getTrackTemplate(resultType);
		final LunarTrackItemStreamGenerator<R> gen            = new LunarTrackItemStreamGenerator<R>(resultType, trans);
		
		this.transform(sourceTemplate,  gen, resultTemplate);
	}

	public <T extends LunarTrackItem, R extends LunarTrackItem> void transform(final Class<T> sourceType, final Func1<Observable<T>, Observable<? extends R>> trans, final Class<R> resultType) {
		final LunarTrack                            sourceTemplate = getTrackTemplate(sourceType);
		final LunarTrackItemStreamTransformer<T, R> transformer    = new LunarTrackItemStreamTransformer<T, R>(sourceType, trans);
		
		this.transform(sourceTemplate,  transformer, resultType);
	}
	
	public void starting(final LunarTrack track) {
		sendReport(LunarPluginStateReport.starting(developerID, track));
	}

	public void running(final LunarTrack track) {
		sendReport(LunarPluginStateReport.running(developerID, track));
	}

	public void stopping(final LunarTrack track) {
		sendReport(LunarPluginStateReport.stopping(developerID, track));
	}

	public void stopping(final LunarTrack track, final Throwable err) {
		sendReport(LunarPluginStateReport.stopping(developerID, track, err));
	}
	
	public void stopped(final LunarTrack track) {
		sendReport(LunarPluginStateReport.stopped(developerID, track));
	}

	public void stopped(final LunarTrack track, final Throwable err) {
		sendReport(LunarPluginStateReport.stopped(developerID, track, err));
	}
	
	private final static Logger LOGGER = LogManager.getLogger();
	private final String hostName;
	private final int    port;
	private final String developerID;
	
	Lunar(final String hostName, final int port, final String developerID) {
		this.hostName    = hostName;
		this.port        = port;
		this.developerID = developerID;
	}
	
	Observable<String> httpRequest(final String path, final Converter<URL, String> method) {
		final URL  url = makeUrl(path);
		
		return Observable.from(url).flatMap(method);	
	}

	<R> Observable<R> httpRequest(final String path, final Converter<URL, String> method, final Class<R> responseType) {
		return httpRequest(path, method).flatMap(jsonString2Object(responseType));
	}
	
	<R, T extends LunarDataResponse<R[]>> Observable<R> getArrayResponse(final String path, final Class<T> responseType, final Class<R> dataType) {
		return httpRequest(path, synchHttpGet, responseType)
				.flatMap(getArrayData(dataType))
				.flatMap(flatten(dataType));
	}

	private URL makeUrl(final String path) {
		try {
			return new URL("http",hostName,port, path);
		} catch (MalformedURLException e) {
			LOGGER.fatal("Unexpected MalformedURLException for {}:{}{} Stack trace: {}", hostName, port, path, e.getStackTrace());
		}
		return null;
	}

	<R, T extends LunarDataResponse<R[]>> Observable<LunarNotify<R>> getNotifyArrayResponse(final String category, final Class<T> responseType, final Class<R> dataType) {
		final String path = String.format("/%s", category);
		return getArrayResponse(path, responseType, dataType)
			   .map(notifyAdd(dataType));
	}
	
	Observable<String> getUpdatesUrl(final String category) {
		return httpRequest(String.format("/updates/%s", category), synchHttpGet, LunarUrlData.Response.class)
				.flatMap(getUrlData);
	}

	<R, T extends LunarStatusUpdateMessage<R>> Observable<LunarNotify<R>> getStatusUpdatesStream(final String category, final Class<T> messageType, final Class<R> dataType) {
		return getMQStream(getUpdatesUrl(category), messageType)
				.flatMap(statusUpdate2Notify(messageType,dataType));
	}
	
	<R extends LunarEntity, T extends LunarStatusUpdateMessage<R>, S extends LunarDataResponse<R[]>> Observable<LunarNotify<R>> getCombinedNotifyStream(final String category, final Class<T> messageType, final Class<S> responseType, final Class<R> dataType) {
		final Observable<LunarNotify<R>> updates = getStatusUpdatesStream(category, messageType, dataType);
		final Observable<LunarNotify<R>> current = getNotifyArrayResponse(category, responseType, dataType);
		//TODO to optimize such that it happens only during the fetch of initial table
		return Observable.merge(current.subscribeOn(Schedulers.newThread()).observeOn(Schedulers.trampoline()), updates)
			   .filter(prematureRemove(dataType)); //filter OUT premature removes if happen
	}
	
	Observable<LunarMQWriter> getOutputTrackStream(final LunarTrack track) {
		return httpRequest(track.streamerRequestPath(developerID), synchHttpGet, LunarUrlData.Response.class)
			   .flatMap(getUrlData)
			   .flatMap(parseMQUrl)
			   .flatMap(connectToServer)
			   .map(createRawWriter);
	}
		
	void sendReport(final LunarPluginStateReport report) {
		//TODO: observable from report: use another version with fixed URL or zip?
		final String json = object2JsonString(LunarPluginStateReport.class).call(report);
		httpRequest("/state/plugins", synchHttpPost(json), LunarResponse.class)
			.flatMap(checkResult(LunarResponse.class))
			.doOnError(
				new Action1<Throwable>() {
					@Override
					public void call(final Throwable err) {
						LOGGER.error("Got an error {} while reporting status {}", err, json);
					}				
				}
		)
		.subscribeOn(Schedulers.newThread())//TODO: quazar or outside of Lunar?
		.observeOn(Schedulers.trampoline())
		.subscribe();
	}

	Observable<byte[]> getInputTrackStream(final LunarTrack sourceTrack) {
		return getMQStream(Observable.from(sourceTrack.url));
	}
}
