package com.cisco.vss.lunar.rx.plugin.core;

public class LunarDataResponse<T> extends LunarResponse {
	public LunarDataResponse() {
		this.result = ResultType.OK;
	}

	public T          data;
}
