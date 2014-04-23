package com.cisco.vss.lunar.rx.mq;

class LunarPrematureEndOfStreamException extends LunarMQException {

	LunarPrematureEndOfStreamException(int sequenceNumber, int bodyLength, int offset) {
		super(String.format(
		 "Premature end of Lunar MQ Stream at record# %d after %d bytes (%d expected)", 
		 	sequenceNumber, 
		 	offset, 
		 	bodyLength));
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
