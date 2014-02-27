package com.cisco.vss.lunar.rx.mq;

public class LunarEndOfStreamException extends LunarMQException {

	public LunarEndOfStreamException() {
		super(true, LunarMQException.StreamingError.LMQ_EOS);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
