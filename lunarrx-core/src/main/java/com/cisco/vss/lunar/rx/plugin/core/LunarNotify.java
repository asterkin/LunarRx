package com.cisco.vss.lunar.rx.plugin.core;

public abstract class LunarNotify<T> {
	private final T item;
	
	public LunarNotify(final T item) {
		this.item = item;
	}
	
	public T getItem() {
		return this.item;
	}
}
