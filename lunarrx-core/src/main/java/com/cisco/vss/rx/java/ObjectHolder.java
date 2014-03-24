package com.cisco.vss.rx.java;

//Useful for testing onError calls
public class ObjectHolder<T> {
	public ObjectHolder(final T value) {
		this.value = value;
	}
	
	public ObjectHolder() {
		this.value = null;
	}

	public T value;
};

