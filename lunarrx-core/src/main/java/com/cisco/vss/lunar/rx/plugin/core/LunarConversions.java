package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.lunar.rx.plugin.core.LunarResponseResult.OK;
import rx.Observable;
import rx.functions.Func1;

import com.cisco.vss.lunar.rx.mq.LunarMQConversions;
import com.cisco.vss.lunar.rx.mq.LunarMQSocket;

public class LunarConversions extends LunarMQConversions {

	public static final <R> Func1<R[], Observable<R>> flatten(Class<R> clazz) {
		return new Func1<R[], Observable<R>>() {
			@Override
			public Observable<R> call(R[] arr) {
				return Observable.from(arr);
			}
		};
	}
	
 	public static final <T extends LunarResponse<R>, R> Converter<T, R[]> getData(Class<R> clazz) {
		return new Converter<T, R[]>() {
			@Override
			protected R[] convert(final T response) throws Throwable {
				if(LunarResponse.ResultType.OK != response.result) throw new Exception("Lunar Response is NOT OK");
				return response.data;
			}
		};
	}
	
	public static final Converter<TrackInfoResponse, TrackInfo> getResultData = new Converter<TrackInfoResponse, TrackInfo>() {
		@Override
		protected TrackInfo convert(final TrackInfoResponse message)	throws Throwable {
			if(OK != message.result) throw new Exception("Lunar Response is NOT OK");
			return message.data[0]; //TODO: more generic?
		}
	};

	public static final Func1<TrackInfo, String> getURL = new Func1<TrackInfo, String>() {
		@Override
		public String call(final TrackInfo info) {
			return info.url;
		}
	};

	public static final Converter<UpdatesTracksResponse, UpdatesTracksResponse.Data> getResultData1 = new Converter<UpdatesTracksResponse, UpdatesTracksResponse.Data>() {
		@Override
		protected UpdatesTracksResponse.Data convert(final UpdatesTracksResponse message)	throws Throwable {
			if(OK != message.result) throw new Exception("Lunar Response is NOT OK");
			return message.data; //TODO: more generic?
		}
	};
	

	public static final Func1<UpdatesTracksResponse.Data, String> getURL1 = new Func1<UpdatesTracksResponse.Data, String>() {
		@Override
		public String call(final UpdatesTracksResponse.Data data) {
			return data.url;
		}
	};

	public static final Func1<TracksStatusUpdate, Boolean> checkStatus(final TrackStatus status) {
		return new Func1<TracksStatusUpdate, Boolean>() {
			@Override
			public Boolean call(final TracksStatusUpdate update) {
				return update.status.equals(status);
			}
		};
	}

	public static final Func1<TracksStatusUpdate, Observable<TrackInfo>> getTracks = new Func1<TracksStatusUpdate, Observable<TrackInfo>>() {
		@Override
		public Observable<TrackInfo> call(final TracksStatusUpdate update) {
			return Observable.from(update.list);
		}
	};
	
	public static final Func1<TrackInfo, Boolean> findTrack(final TrackInfo template) {
		return new Func1<TrackInfo, Boolean>() {
			@Override
			public Boolean call(final TrackInfo info) {
				return    info.sourceID.equals(template.sourceID)
					   && info.pluginName.equals(template.pluginName)
					   && info.trackName.equals(template.trackName);
			}
		};
	}

	public static final Func1<LunarMQSocket, LunarMQWriter> createRawWriter = new Func1<LunarMQSocket, LunarMQWriter>() {

		@Override
		public LunarMQWriter call(final LunarMQSocket socket) {
			return new LunarMQWriter(socket);
		}
		
	};
}
