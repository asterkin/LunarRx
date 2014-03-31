package com.cisco.vss.lunar.rx.plugin.core;

public class LunarSource implements LunarEntity {
	public LunarSource(int sourceID, String sourceName) {
		this.sourceID = sourceID;
		this.name     = sourceName;
	}
	public LunarSource() {}
	
	//Primarily for testing purposes
	@Override
	public boolean equals(final Object obj) {
		if (null == obj) return false;
		if (this == obj) return true;
		if (! (obj instanceof LunarSource)) return false;
		final LunarSource that = (LunarSource) obj;
		return (this.sourceID == that.sourceID) && this.name.equals(that.name);
	}

	public int    sourceID;
	public String name;
	
	public class Response            extends LunarResponse<LunarSource[]> {}
	public class StatusUpdateMessage extends LunarStatusUpdateMessage<LunarSource> {}
	
	@Override
	public Long getId() {
		return new Long(sourceID);
	}
}
