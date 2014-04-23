package com.cisco.vss.lunar.rx.mq;

class LunarEndOfStreamException extends LunarMQException {

	LunarEndOfStreamException() {
		super(true, LunarMQException.StreamingError.LMQ_EOS);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
