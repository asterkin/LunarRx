package com.cisco.vss.lunar.rx.plugin.core;

public class LunarResponse<T> {
	public enum ResultType {
		OK,
		NOT_OK
	}
	public ResultType result;
	public String     message;
	public T[]        data;
}
