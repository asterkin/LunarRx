package com.cisco.vss.lunar.rx.plugin.core;

public class LunarResponse {

	public enum ResultType {
		OK,
		NOT_OK
	}

	public LunarResponse() {
		result = ResultType.OK;
	}
	
	public ResultType result;
	public String     message;

}
