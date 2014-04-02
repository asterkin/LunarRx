package com.cisco.vss.rx.java;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import rx.Observable;
import rx.Subscriber;
import rx.functions.*;

public class Conversions {
    public final static Func1<byte[], String> byte2String = new Func1<byte[], String>() {
       	@Override
        public String call(final byte[] message) {
       		return new String(message);
        }      
    };

    public final static Func1<String, byte[]> string2Byte = new Func1<String, byte[]>() {
       	@Override
        public byte[] call(final String message) {
       		return message.getBytes();
        }      
    };
    
	public static abstract class Converter<T1,T2> implements Func1<T1, Observable<T2>> {
		private final static Logger LOGGER = LogManager.getLogger();

	    @Override
	    public Observable<T2> call(final T1 message) {
	    	final Observable.OnSubscribe<T2> func = new Observable.OnSubscribe<T2>() {
	    		@Override
	    		public void call(Subscriber<? super T2> observer) {
	    			try {
	    				LOGGER.trace("Converting: {}", message);
	    				final T2 result = convert(message);
	    				LOGGER.trace("Conversion Result: {}", result);
	    				observer.onNext(result);
	    				observer.onCompleted();
	    			} catch(Throwable err) {
	    				LOGGER.error("Got an error during conversion: {}", err.toString());
	    				observer.onError(err);
	    			}
	    		}
	        };
	        return Observable.create(func);
	    }

	    protected abstract T2 convert(T1 message) throws Throwable;	    
	    
	};

	static private final JsonSerializer<Date> ser = new JsonSerializer<Date>() {

		@Override
		public JsonElement serialize(Date src, Type typeOfSrc,
				JsonSerializationContext context) {
		    return src == null ? null : new JsonPrimitive(src.getTime());
		}
	};
	
	static private final JsonDeserializer<Date> deser = new JsonDeserializer<Date>() {

		@Override
		public Date deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context)
				throws JsonParseException {
			return json == null ? null : new Date(json.getAsLong());			}
	};

	static private final Gson gson = new GsonBuilder()
	   .registerTypeAdapter(Date.class, ser)
	   .registerTypeAdapter(Date.class, deser).create();
	
	public static <R> Converter<String,R> jsonString2Object(final Class<R> clazz) {
		return new Converter<String, R>() {
			protected R convert(String message) throws Throwable {
				return gson.fromJson(message, clazz);
			}
		};
	}

	public static <T> Func1<T, String> object2JsonString(final Class<T> clazz) {
		return new Func1<T, String>() {
			@Override
			public String call(T src) {
				return gson.toJson(src);
			}
		};
	}
	
	public static Converter<String, Matcher> parseString(final String type, final String sPattern) {
		final Pattern pattern = Pattern.compile(sPattern);
		
		return new Converter<String, Matcher>() {
			@Override
			protected Matcher convert(String message) throws Throwable {
				final Matcher matcher = pattern.matcher(message);
				if(!matcher.matches()) 
					throw new Exception(String.format(
						"String '%s' does not match the '%s' pattern",
						message,
						type)
					);
				return matcher;
			}
			
		};
	}
	
	public static final Converter<URL, String> synchHttpGet = new Converter<URL, String>() {
		@Override
		protected String convert(URL url) throws Throwable {
			final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.addRequestProperty("Accept", "application/json"); //TODO: support for more encodings
			if (connection.getResponseCode() != 200)
				throw new Exception("Got HTTP error code [" + connection.getResponseCode() + "]");
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String        inputLine;
			StringBuilder result = new StringBuilder();

			while ((inputLine = in.readLine()) != null)
				result.append(inputLine);
			in.close();
			connection.disconnect();				
			return result.toString();
		}
	};
	
	public static Converter<URL, String> synchHttpPost(final String message) {
		return new Converter<URL, String>() {
			@Override
			protected String convert(URL url) throws Throwable {
				final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setRequestMethod("POST");
	            connection.addRequestProperty("Content-Type", "application/json");
	            connection.setDoOutput(true);
				final OutputStream out = connection.getOutputStream();
				out.write(message.getBytes());
				out.flush();
				//TODO: common with GET
				if (connection.getResponseCode() != 200)
					throw new Exception("Got HTTP error code [" + connection.getResponseCode() + "]");
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String        inputLine;
				StringBuilder result = new StringBuilder();

				while ((inputLine = in.readLine()) != null)
					result.append(inputLine);
				in.close();
				connection.disconnect();				
				return result.toString();
			}
			
		};
	}
	
	//TODO: why I cannot get it from RxJava?
 	public static final <R> Func1<R[], Observable<R>> flatten(Class<R> clazz) {
 		return new Func1<R[], Observable<R>>() {
			@Override
			public Observable<R> call(final R[] arr) {
				return Observable.from(arr);
			}
 		};
	};
};
