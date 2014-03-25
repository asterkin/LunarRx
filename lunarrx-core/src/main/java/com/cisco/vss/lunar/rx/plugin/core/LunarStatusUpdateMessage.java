package com.cisco.vss.lunar.rx.plugin.core;

import com.google.gson.annotations.SerializedName;

public class LunarStatusUpdateMessage<T> {
	public enum Status {
		@SerializedName("up")
		UP,
		@SerializedName("down")
		DOWN
	}

	public Status status;
	public T[]    list;
}
