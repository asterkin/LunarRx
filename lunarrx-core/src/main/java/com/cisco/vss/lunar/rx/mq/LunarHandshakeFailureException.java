package com.cisco.vss.lunar.rx.mq;

public class LunarHandshakeFailureException extends LunarMQException {

	public LunarHandshakeFailureException(final String response) {
		super(String.format("Handshake failure. Got [%s] error message", response), false);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
