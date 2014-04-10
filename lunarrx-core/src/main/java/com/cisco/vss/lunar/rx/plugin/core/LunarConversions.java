package com.cisco.vss.lunar.rx.plugin.core;

import static com.cisco.vss.lunar.rx.plugin.core.LunarResponseResult.OK;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Observable;
import rx.functions.Func1;

import com.cisco.vss.lunar.rx.mq.LunarMQConversions;
import com.cisco.vss.lunar.rx.mq.LunarMQSocket;

public class LunarConversions extends LunarMQConversions {

 	public static final <T extends LunarResponse> Converter<T, T> checkResult(Class<T> clazz) {
		return new Converter<T, T>("checkResult") {
			@Override
			protected T convert(final T response) throws Throwable {
				if(LunarResponse.ResultType.OK != response.result) throw new Exception("Lunar Response is NOT OK: "+response.message);
				return response;
			}
		};
	}
	
 	public static final <T extends LunarDataResponse<R[]>, R> Converter<T, R[]> getArrayData(Class<R> clazz) {
		return new Converter<T, R[]>("getArrayData") {
			@Override
			protected R[] convert(final T response) throws Throwable {
				if(LunarResponse.ResultType.OK != response.result) throw new Exception("Lunar Response is NOT OK");
				return response.data;
			}
		};
	}

 	public static final Converter<LunarDataResponse<LunarUrlData>, String> getUrlData = new Converter<LunarDataResponse<LunarUrlData>, String>("getUrlData") {
		@Override
		protected String convert(final LunarDataResponse<LunarUrlData> response) throws Exception {
			if(LunarResponse.ResultType.OK != response.result) throw new Exception("Lunar Response is NOT OK");
			return response.data.url;
		}
 	};
 	
 	public static final <T> Func1<T, LunarNotify<T>> notifyAdd(final Class<T> clazz) {
 		return new Func1<T, LunarNotify<T>>() {
			@Override
			public LunarNotify<T> call(T item) {
				return new LunarAdd<T>(item);
			} 			
 		};
 	}

 	public static final <T> Func1<T, LunarNotify<T>> notifyRemove(final Class<T> clazz) {
 		return new Func1<T, LunarNotify<T>>() {
			@Override
			public LunarNotify<T> call(T item) {
				return new LunarRemove<T>(item);
			} 			
 		};
 	}
 	
 	public static <R, T extends LunarStatusUpdateMessage<R>> Func1<T , Observable<LunarNotify<R>>> statusUpdate2Notify(final Class<T> messageType, final Class<R> dataType) {
 		return new Func1<T, Observable<LunarNotify<R>>>() {
			@Override
			public Observable<LunarNotify<R>> call(final T message) {
				switch(message.status) {
				case UP:
					return Observable.from(message.list).map(notifyAdd(dataType));
				case DOWN:
					return Observable.from(message.list).map(notifyRemove(dataType));
				default:
					return null; //should not get there, but do not want to throw an exception
				}
			}
 		};
 	}
 	
 	public static <T extends LunarEntity> Func1<LunarNotify<T>, Boolean> prematureRemove(final Class<T> clazz) {
		return new Func1<LunarNotify<T>, Boolean>() {
			private final Set<Long> added    = new HashSet<Long>();
			private final Set<Long> deleting = new HashSet<Long>();
			private final Logger    LOGGER   = LogManager.getLogger(String.format("LunarConversions.prematureRemove<%s>", this.getClass().getName()));
			@Override
			public Boolean call(final LunarNotify<T> notify) {
				final Long id = notify.getItem().getId();
				Boolean    rc = false;
				if(notify instanceof LunarAdd<?>)
					if(deleting.contains(id)) {
						LOGGER.info("Premature remove resolved for id={}", id);
						deleting.remove(id);
					} else {
						added.add(id);
						rc = true;
					}
				else if(notify instanceof LunarRemove<?>) {
					if(added.contains(id)) {
						added.remove(id);
						rc = true;
					} else {
						LOGGER.info("Premature remove detected for id={}", id);
						deleting.add(id);
					}
				}
				return rc;
			}
		};		
 	}

	public static final Func1<LunarNotify<LunarTrack>, Boolean> pluginTrack(final LunarTrack template) {
		return new Func1<LunarNotify<LunarTrack>, Boolean>() {
			@Override
			public Boolean call(final LunarNotify<LunarTrack> notify) {
				final LunarTrack track = notify.getItem();
				return    ((template.sourceID  ==null)||(template.sourceID.equals(track.sourceID)))
					   && ((template.pluginName==null)||(template.pluginName.equals(track.pluginName)))
					   && ((template.trackName ==null)||(template.trackName.equals(track.trackName)));
			}
		};
	}
 	
	public static <R extends TrackItem> Func1<R, R> setTrackDetails(final LunarTrack resultTrack, final Class<R> resultType) {
		return new Func1<R, R>() {
			@Override
			public R call(final R item) {
				item.sourceID   = resultTrack.sourceID;
				item.pluginName = resultTrack.pluginName;
				item.trackName  = resultTrack.trackName;
				return item;
			}
			
		};
	}
	
 	//So far new App API
	public static final Converter<TrackInfoResponse, LunarTrack> getResultData = new Converter<TrackInfoResponse, LunarTrack>("getResultData") {
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

	public static final Converter<UpdatesTracksResponse, UpdatesTracksResponse.Data> getResultData1 = new Converter<UpdatesTracksResponse, UpdatesTracksResponse.Data>("getResultData1") {
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
