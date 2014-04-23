package com.cisco.vss.lunar.rx.mq;

class LunarHandshakeFailureException extends LunarMQException {

	LunarHandshakeFailureException(final String response) {
		super(String.format("Handshake failure. Got [%s] error message", response));
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
