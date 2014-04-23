package com.cisco.vss.lunar.rx.plugin.core;

class LunarResponse {

	enum ResultType {
		OK,
		NOT_OK
	}

	LunarResponse() {
		result = ResultType.OK;
	}
	
	ResultType result;
	String     message;

}
