package com.cisco.vss.lunar.rx.mq;

class LunarInvalidInputHeaderFormatException extends LunarMQException {

	LunarInvalidInputHeaderFormatException(int code) {
		super(String.format("Invalid character code in Header: %d", code));
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
