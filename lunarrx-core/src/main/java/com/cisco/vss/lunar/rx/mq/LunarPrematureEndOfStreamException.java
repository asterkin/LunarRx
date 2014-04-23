package com.cisco.vss.lunar.rx.mq;

public class LunarPrematureEndOfStreamException extends LunarMQException {

	public LunarPrematureEndOfStreamException(int sequenceNumber, int bodyLength, int offset) {
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
