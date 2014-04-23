package com.cisco.vss.lunar.rx.mq;

import static com.cisco.vss.lunar.rx.mq.LunarMQException.StreamingError.*;

class LunarMessagesLostException extends LunarMQException {

	LunarMessagesLostException(final int lostCount) {
		super(
			String.format("%d LunarMQ messages were lost - are you working too slowly?", lostCount),
			true,
			LMQ_UNKNOWN
		);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
