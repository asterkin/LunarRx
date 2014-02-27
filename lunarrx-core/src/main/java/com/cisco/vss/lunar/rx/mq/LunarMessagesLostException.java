package com.cisco.vss.lunar.rx.mq;

public class LunarMessagesLostException extends LunarMQException {

	public LunarMessagesLostException(final int lostCount) {
		super(
			String.format("%d LunarMQ messages were lost - are you working too slowly?", lostCount),
			true
		);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
