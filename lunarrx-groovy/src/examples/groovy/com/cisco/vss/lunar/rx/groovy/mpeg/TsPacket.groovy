package com.cisco.vss.lunar.rx.groovy.mpeg

class TsPacket {
	private final byte[] packet
	
	public TsPacket(final byte[] packet) {
		this.packet = packet
	}
	
	public Boolean hasError() {
		return (0 != (packet[1] & 0x80))
	}
	
	public Boolean hasPayloadStart() {
		return (0 != (packet[1] & 0x40))
	}
	
	public Integer pid() {
		return ((packet[1]&0x1F) << 8) | packet[2]
	}
	
	public byte[] payload() {
		def offset = payloadOffset()
		
		return packet[offset .. -1]
	  }
	  
	  private Integer payloadOffset() {
		return (adaptationFieldExist()) ? 4+packet[4] : 4
	  }
	  
	  private Boolean adaptationFieldExist() {
		return 0 != (packet[3] & 0x20)
	  }
}
