package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.lunar.rx.mq.LunarMQConversions.*;
import static com.cisco.vss.lunar.rx.plugin.core.LunarConversions.*;
import static com.cisco.vss.lunar.rx.plugin.core.LunarTrackTemplateFactory.getTrackTemplate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cisco.vss.lunar.rx.mq.LunarMQWriter;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class LunarPlugin {
	public LunarPlugin(final String[] args) {
	    //   HOST     PORT                       DEVELOPER_ID  SOURCE_ID
		this(args[2] ,Integer.parseInt(args[3]), args[0],      Integer.parseInt(args[4]));
	}

	public void transform(final LunarTrack sourceTrack, final Func2<Observable<byte[]>, LunarTrack, Observable<? extends byte[]>> trans, final LunarTrack resultTrack) {
		sourceTrack.sourceID = sourceID;
		resultTrack.sourceID = sourceID;
		final Observable<? extends byte[]> resultObs = getTransformedStream(sourceTrack, trans, resultTrack); 
		final Observable<LunarMQWriter>    outputObs = getOutputTrackStream(resultTrack);
		
		starting(resultTrack);
		outputObs.subscribe(
			new Action1<LunarMQWriter>() {
				@Override
				public void call(final LunarMQWriter writer) {
					running(resultTrack);
					resultObs
					.flatMap(writer)
					.subscribe(
						new Action1<byte[]>() {
							@Override
							public void call(final byte[] buffer) {
								LOGGER.debug("Sent {} to {}", buffer, resultTrack);
							}
						},
						new Action1<Throwable>() {
							@Override
							public void call(final Throwable err) {
								LOGGER.error("Got an error while generating {}", resultTrack, err.fillInStackTrace());
								error(resultTrack, err);
								closeSocket(resultTrack, writer);
							}
						},
						new Action0() {
							@Override
							public void call() {
								closeSocket(resultTrack, writer);
							}					
						}						
					);
				}				
			},
			new Action1<Throwable>() {
				@Override
				public void call(final Throwable err) {
					LOGGER.error("Got an error while acquiring an ouput stream for {}", resultTrack, err.fillInStackTrace());
					error(resultTrack, err);
					stopped(resultTrack);
				}				
			}
		);
	}

	private void closeSocket(final LunarTrack resultTrack, final LunarMQWriter writer) {
		try {
			stopping(resultTrack);
			writer.close();
			stopped(resultTrack);
		} catch (final IOException err) {
			LOGGER.error("Got an error while closing MQ socket for {}", resultTrack, err.fillInStackTrace());
		}		
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

	private final static  Logger LOGGER = LogManager.getLogger();
	private final String         hostName;
	private final int            port;
	private final String         developerID;
	private final Integer        sourceID;
	
	LunarPlugin(final String hostName, final int port, final String developerID, final Integer sourceID) {
		this.hostName    = hostName;
		this.port        = port;
		this.developerID = developerID;
		this.sourceID    = sourceID;
	}

	Observable<LunarMQWriter> getOutputTrackStream(final LunarTrack track) {
		return httpRequest(track.streamerRequestPath(developerID), synchHttpGet, LunarUrlData.Response.class)
			   .map(getUrlData)
			   .flatMap(parseMQUrl)
			   .flatMap(connectToServer)
			   .map(createRawWriter);
	}
		
	Observable<String> httpRequest(final String path, final Converter<URL, String> method) {
		final URL  url = makeUrl(path);
		
		return Observable.from(url).flatMap(method);	
	}

	<R extends LunarResponse> Observable<R> httpRequest(final String path, final Converter<URL, String> method, final Class<R> responseType) {
		return httpRequest(path, method)
				.flatMap(jsonString2Object(responseType))
				.flatMap(checkResult(responseType));
	}
	
	private URL makeUrl(final String path) {
		try {
			return new URL("http",hostName,port, path);
		} catch (MalformedURLException e) {
			LOGGER.fatal("Unexpected MalformedURLException for {}:{}{} Stack trace: {}", hostName, port, path, e.getStackTrace());
		}
		return null;
	}

	Observable<? extends byte[]> getTransformedStream(final LunarTrack sourceTrack, final Func2<Observable<byte[]>, LunarTrack, Observable<? extends byte[]>> trans, final LunarTrack resultTrack) {
		return trans.call(getInputTrackStream(sourceTrack), resultTrack);
	}

	public Observable<byte[]> getInputTrackStream(final LunarTrack sourceTrack) {
		return getArrayResponse(sourceTrack.httpGetRequestPath(), LunarTrack.Response.class, LunarTrack.class)
		.take(1)
		.map(getUrl)
		.flatMap(parseMQUrl)
		.flatMap(connectToServer)
		.flatMap(readStream);
	}
	
	<R, T extends LunarDataResponse<R[]>> Observable<R> getArrayResponse(final String path, final Class<T> responseType, final Class<R> dataType) {
		return httpRequest(path, synchHttpGet, responseType)
				.map(getArrayData(dataType))
				.flatMap(flatten(dataType));
	}
	
	void sendReport(final LunarPluginStateReport report) {
		//TODO: observable from report: use another version with fixed URL or zip?
		final String json = object2JsonString(LunarPluginStateReport.class).call(report);
		httpRequest("/state/plugins", synchHttpPost(json), LunarResponse.class)
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

	void starting(final LunarTrack track) {
		sendReport(LunarPluginStateReport.starting(developerID, track));
	}

	void running(final LunarTrack track) {
		sendReport(LunarPluginStateReport.running(developerID, track));
	}

	void stopping(final LunarTrack track) {
		sendReport(LunarPluginStateReport.stopping(developerID, track));
	}

	void stopped(final LunarTrack track) {
		sendReport(LunarPluginStateReport.stopped(developerID, track));
	}

	void error(final LunarTrack track, final Throwable err) {
		sendReport(LunarPluginStateReport.error(developerID, track, err));
	}
	
}
