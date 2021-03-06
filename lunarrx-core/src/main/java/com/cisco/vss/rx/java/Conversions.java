package com.cisco.vss.rx.java;

import java.io.BufferedReader;
import java.io.IOException;
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
		protected final Logger LOGGER;

		public Converter(final String conversionType) {
			final String name = String.format("%s(%s)", this.getClass().getName(), conversionType);
			LOGGER = LogManager.getLogger(name);
		}
		
	    @Override
	    public Observable<T2> call(final T1 message) {
	    	final Observable.OnSubscribe<T2> func = new Observable.OnSubscribe<T2>() {
	    		@Override
	    		public void call(Subscriber<? super T2> observer) {
	    			try {
	    				LOGGER.debug("Converting: {}", message);
	    				final T2 result = convert(message);
	    				LOGGER.debug("Result: {}", result);
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

	static private final JsonSerializer<Date> dateSer = new JsonSerializer<Date>() {

		@Override
		public JsonElement serialize(Date src, Type typeOfSrc,
				JsonSerializationContext context) {
		    return src == null ? null : new JsonPrimitive(src.getTime());
		}
	};
	
	static private final JsonDeserializer<Date> dateDeser = new JsonDeserializer<Date>() {

		@Override
		public Date deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context)
				throws JsonParseException {
			return json == null ? null : new Date(json.getAsLong());			}
	};

	private static final String HEXES = "0123456789ABCDEF";
	
	static private final JsonSerializer<byte[]> byteSer = new JsonSerializer<byte[]>() {
		@Override
		public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
	        if (src == null) return null;
	        final StringBuilder hex = new StringBuilder(2 * src.length);
	        for (final byte b : src)
	        {
	            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
	        }
	        return new JsonPrimitive(hex.toString());		
	    }
	};
	
	static private final JsonDeserializer<byte[]> byteDeser = new JsonDeserializer<byte[]>() {
		@Override
		public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			final String s = json.getAsString();
		    int len = s.length();
		    byte[] data = new byte[len / 2];
		    for (int i = 0; i < len; i += 2)
		    {
		       data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
		                               + Character.digit(s.charAt(i+1), 16));
		    }
		    return data;		
		}
	};
	
	static private final Gson gson = new GsonBuilder()
	   .registerTypeAdapter(Date.class, dateSer)
	   .registerTypeAdapter(Date.class, dateDeser)
	   .registerTypeAdapter(byte[].class, byteSer)
	   .registerTypeAdapter(byte[].class, byteDeser).create();
	
	public static <R> Converter<String,R> jsonString2Object(final Class<R> clazz) {
		return new Converter<String, R>("jsonString2Object") {
			protected R convert(String message) throws Throwable {
				return gson.fromJson(message, clazz);
			}
		};
	}

	public static <T> Func1<T, String> object2JsonString(final Class<T> clazz) {
		return new Func1<T, String>() {
			final String name   = String.format("%s(object2JsonString)", this.getClass().getName());
			final Logger LOGGER = LogManager.getLogger(name);
			@Override
			public String call(T src) {
				final String result = gson.toJson(src);
				
				LOGGER.debug("Converted: {} to: {}", src, result);
				return result;
			}
		};
	}
	
	public static Converter<String, Matcher> parseString(final String type, final String sPattern) {
		final Pattern pattern = Pattern.compile(sPattern);
		
		return new Converter<String, Matcher>("parseString") {
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

	private static String readHttpResponse(final HttpURLConnection connection) throws IOException, Exception {
		if (connection.getResponseCode() != 200)
			throw new Exception("Got HTTP error code [" + connection.getResponseCode() + "]");
		
		BufferedReader in     = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder  result = new StringBuilder();
		String         inputLine;

		while ((inputLine = in.readLine()) != null)
			result.append(inputLine);
		
		in.close();
		connection.disconnect();				
		return result.toString();
	}
	
	public static final Converter<URL, String> synchHttpGet = new Converter<URL, String>("synchHttpGet") {
		@Override
		protected String convert(URL url) throws Throwable {
			final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.addRequestProperty("Accept", "application/json"); //TODO: support for more encodings
			return readHttpResponse(connection);
		}

	};
	
	public static Converter<URL, String> synchHttpPost(final String message) {
		return new Converter<URL, String>("synchHttpPost") {
			@Override
			protected String convert(URL url) throws Throwable {
				final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setRequestMethod("POST");
	            connection.addRequestProperty("Content-Type", "application/json");
	            connection.setDoOutput(true);
				final OutputStream out = connection.getOutputStream();
				out.write(message.getBytes());
				out.flush();
				return readHttpResponse(connection);
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
};
