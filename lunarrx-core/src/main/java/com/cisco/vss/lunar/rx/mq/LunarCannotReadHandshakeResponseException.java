package com.cisco.vss.lunar.rx.mq;

public class LunarCannotReadHandshakeResponseException extends LunarMQException {

	public LunarCannotReadHandshakeResponseException() {
		super("Cannot read Lunar MQ handshake response", false);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
