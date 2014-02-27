package com.cisco.vss.lunar.rx.mq;

public class LunarPrematureEndOfStreamException extends LunarMQException {

	public LunarPrematureEndOfStreamException() {
		super("Premature end of Lunar MQ Stream", false);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
