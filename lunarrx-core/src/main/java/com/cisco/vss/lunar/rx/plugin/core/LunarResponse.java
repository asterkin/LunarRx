package com.cisco.vss.lunar.rx.plugin.core;

public class LunarResponse<T> {
	public LunarResponse() {
		this.result = ResultType.OK;
	}
	public LunarResponse(final T[] data) {
		this.result = ResultType.OK;
		this.data   = data;
	}
	public enum ResultType {
		OK,
		NOT_OK
	}
	public ResultType result;
	public String     message;
	public T[]        data;
}
