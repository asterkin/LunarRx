package com.cisco.vss.lunar.rx.mq;

public class LunarEndOfStreamWhileReadingHeaderException extends LunarMQException {
	public LunarEndOfStreamWhileReadingHeaderException() {
		super("Unexpected end of steram while reading Lunar MQ header", false);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
