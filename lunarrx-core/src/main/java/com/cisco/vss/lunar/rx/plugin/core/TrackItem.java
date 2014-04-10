package com.cisco.vss.lunar.rx.plugin.core;

import java.util.Date;
import com.google.gson.annotations.SerializedName;

public abstract class TrackItem
{
    public Date     time; //TODO: setters?
    @SerializedName("source")
    public int      sourceID;
    @SerializedName("vendor")
    public String   pluginName;
    @SerializedName("track")
    public String   trackName;
    @SerializedName("version")
    public int      trackVersion;
 
    public TrackItem() {
    	this.time         = new Date();
    	this.trackVersion = 1; //TODO
    }
    
    public TrackItem(int sourceID, Date time, String pluginName, String trackName, int trackVersion)
    {
	    this.time         = time;
	    this.sourceID     = sourceID;
        this.pluginName   = pluginName;
	    this.trackName    = trackName;
        this.trackVersion = trackVersion;	
    }
    
    public void shiftBackInTime(int milliseconds)
    {
        time.setTime(time.getTime() - milliseconds);
    }

    public Date getTime() {
    	return time;
    }
    
    public int getSourceID()
    {
        return sourceID;
    }

	public String getTrackName() {
		return trackName;
	}

	public int getTrackVersion() {
		return trackVersion;
	}

	public String getPluginName() {
		return pluginName;
	}
}