package com.cisco.vss.lunar.rx.plugin.core;

import com.google.gson.annotations.SerializedName;

public enum TrackStatus {
	@SerializedName("up")
    TRACK_IS_UP,
    @SerializedName("down")
    TRACK_IS_DOWN;
    
}

