package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.lunar.rx.plugin.core.LunarResponseResult.OK;
import rx.Observable;
import rx.functions.Func1;

import com.cisco.vss.lunar.rx.mq.LunarMQConversions;
import com.cisco.vss.lunar.rx.mq.LunarMQSocket;

public class LunarConversions extends LunarMQConversions {

 	public static final <T extends LunarResponse<R[]>, R> Converter<T, R[]> getArrayData(Class<R> clazz) {
		return new Converter<T, R[]>() {
			@Override
			protected R[] convert(final T response) throws Throwable {
				if(LunarResponse.ResultType.OK != response.result) throw new Exception("Lunar Response is NOT OK");
				return response.data;
			}
		};
	}

 	public static final <R> Func1<R[], Observable<R>> flatten(Class<R> clazz) {
 		return new Func1<R[], Observable<R>>() {
			@Override
			public Observable<R> call(final R[] arr) {
				return Observable.from(arr);
			}
 		};
	};
 	
 	public static final Converter<LunarResponse<LunarUrlData>, String> getUrlData = new Converter<LunarResponse<LunarUrlData>, String>() {
		@Override
		protected String convert(final LunarResponse<LunarUrlData> response) throws Exception {
			if(LunarResponse.ResultType.OK != response.result) throw new Exception("Lunar Response is NOT OK");
			return response.data.url;
		}
 	};
 	
 	public static final <T> Func1<T, LunarNotify<T>> notifyAdd(Class<T> clazz) {
 		return new Func1<T, LunarNotify<T>>() {
			@Override
			public LunarNotify<T> call(T item) {
				return new LunarAdd<T>(item);
			} 			
 		};
 	}
 	
 	//So far new App API
	public static final Converter<TrackInfoResponse, LunarTrack> getResultData = new Converter<TrackInfoResponse, LunarTrack>() {
		@Override
		protected LunarTrack convert(final TrackInfoResponse message)	throws Throwable {
			if(OK != message.result) throw new Exception("Lunar Response is NOT OK");
			return message.data[0]; //TODO: more generic?
		}
	};

	public static final Func1<LunarTrack, String> getURL = new Func1<LunarTrack, String>() {
		@Override
		public String call(final LunarTrack info) {
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

	public static final Func1<TracksStatusUpdate, Observable<LunarTrack>> getTracks = new Func1<TracksStatusUpdate, Observable<LunarTrack>>() {
		@Override
		public Observable<LunarTrack> call(final TracksStatusUpdate update) {
			return Observable.from(update.list);
		}
	};
	
	public static final Func1<LunarTrack, Boolean> findTrack(final LunarTrack template) {
		return new Func1<LunarTrack, Boolean>() {
			@Override
			public Boolean call(final LunarTrack info) {
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
