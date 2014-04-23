package com.cisco.vss.lunar.rx.plugin.core;

class LunarDataResponse<T> extends LunarResponse {
	LunarDataResponse() {
		this.result = ResultType.OK;
	}

	T          data;
}
