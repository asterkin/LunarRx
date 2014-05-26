package com.cisco.vss.lunar.rx.plugin.core;

import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.functions.Func1;
import com.cisco.vss.lunar.rx.mq.LunarMQConversions;

class LunarConversions extends LunarMQConversions {

 	static final <T extends LunarResponse> Converter<T, T> checkResult(Class<T> clazz) {
		return new Converter<T, T>("checkResult") {
			@Override
			protected T convert(final T response) throws Throwable {
				if(LunarResponse.ResultType.OK != response.result) throw new Exception("Lunar Response is NOT OK: "+response.message);
				return response;
			}
		};
	}
	
 	static final <T extends LunarDataResponse<R[]>, R> Func1<T, R[]> getArrayData(Class<R> clazz) {
		return new Func1<T, R[]>() {
			@Override
			public R[] call(final T response) {
				return response.data;
			}
		};
	}

 	static final Func1<LunarDataResponse<LunarUrlData>, String> getUrlData = new Func1<LunarDataResponse<LunarUrlData>, String>() {
		@Override
		public String call(final LunarDataResponse<LunarUrlData> response) {
			return response.data.url;
		}
 	};

 	static final Func1<LunarTrack, String> getUrl = new Func1<LunarTrack, String>() {
		@Override
		public String call(final LunarTrack track) {
			return track.url;
		}
 	};
 	
 	static final <T> Func1<T, LunarNotify<T>> notifyAdd(final Class<T> clazz) {
 		return new Func1<T, LunarNotify<T>>() {
			@Override
			public LunarNotify<T> call(T item) {
				return new LunarAdd<T>(item);
			} 			
 		};
 	}

 	static final <T> Func1<T, LunarNotify<T>> notifyRemove(final Class<T> clazz) {
 		return new Func1<T, LunarNotify<T>>() {
			@Override
			public LunarNotify<T> call(T item) {
				return new LunarRemove<T>(item);
			} 			
 		};
 	}
 	
 	//
 	//Non-trivial transformation and exception is unlikely to happen, hence Func1 rather than Converter
 	//
 	static <R, T extends LunarStatusUpdateMessage<R>> Func1<T , Observable<LunarNotify<R>>> statusUpdate2Notify(final Class<T> messageType, final Class<R> dataType) {
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
 	
 	static <T extends LunarEntity> Func1<LunarNotify<T>, Boolean> prematureRemove(final Class<T> clazz) {
		return new Func1<LunarNotify<T>, Boolean>() {
			private final Set<String> added    = new HashSet<String>();
			private final Set<String> deleting = new HashSet<String>();
			private final Logger      LOGGER   = LogManager.getLogger(String.format("LunarConversions.prematureRemove<%s>", this.getClass().getName()));
			@Override
			public Boolean call(final LunarNotify<T> notify) {
				final String id = notify.getItem().getId();
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

	static final Func1<LunarNotify<LunarTrack>, Boolean> pluginTrack(final LunarTrack template) {
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
 	
	static <R extends LunarTrackItem> Func1<R, R> setTrackDetails(final LunarTrack resultTrack, final Class<R> resultType) {
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
}
