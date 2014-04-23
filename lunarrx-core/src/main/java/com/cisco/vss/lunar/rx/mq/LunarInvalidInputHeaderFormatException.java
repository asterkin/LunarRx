package com.cisco.vss.lunar.rx.mq;

public class LunarInvalidInputHeaderFormatException extends LunarMQException {

	public LunarInvalidInputHeaderFormatException(int code) {
		super(String.format("Invalid character code in Header: %d", code));
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
