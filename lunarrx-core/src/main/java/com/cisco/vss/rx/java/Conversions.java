package com.cisco.vss.rx.java;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
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
    
	public static abstract class Converter<T1,T2> implements Func1<T1, Observable<T2>> {

	    @Override
	    public Observable<T2> call(final T1 message) {
	    	final Observable.OnSubscribe<T2> func = new Observable.OnSubscribe<T2>() {
	    		@Override
	    		public void call(Subscriber<? super T2> observer) {
	    			try {
	    				observer.onNext(convert(message));
	    				observer.onCompleted();
	    			} catch(Throwable err) {
	    				observer.onError(err);
	    			}
	    		}
	        };
	        return Observable.create(func);
	    }

	    protected abstract T2 convert(T1 message) throws Throwable;	    
	    
	};
	
	public static <R> Converter<String,R> jsonString2Object(final Class<R> clazz) {
		return new Converter<String, R>() {
			final Gson gson = new Gson();
			protected R convert(String message) throws Throwable {
				return gson.fromJson(message, clazz);
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
	
	public static Converter<URL, String> synchHttpGet = new Converter<URL, String>() {
		@Override
		protected String convert(URL url) throws Throwable {
			final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.addRequestProperty("Accept", "application/json"); //TODO: support for more encodings
			if (connection.getResponseCode() != 200)
				throw new Exception("Got HTTP error code [" + connection.getResponseCode() + "]");
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			String result = "";

			while ((inputLine = in.readLine()) != null)
				result += inputLine;
			in.close();
			connection.disconnect();				
			return result;
		}
	};
	
	public static Converter<String[], URL> makeURL = new Converter<String[], URL>() {
		@Override
		protected URL convert(String[] args) throws Throwable {
			final String file = "/"+args[3] + formatUrlArgs(args);
			return new URL(args[0],args[1],Integer.parseInt(args[2]),file);
		}

		private String formatUrlArgs(String[] args) {
		    String argsString = "";
	        boolean isFirstParam = true;
	        for (int i=4; i < args.length-1; i += 2)
	        {
	            if (isFirstParam)
	            {
	                argsString += "?";
	                isFirstParam = false;
	            }
	            else
	            {
	                argsString += "&";
	            }
	            argsString += args[i] + "=" + args[i+1];
	        }
	        return argsString;
		}		
	};
};
