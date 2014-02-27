package com.cisco.vss.lunar.rx.plugin;

import java.util.Date;
import com.google.gson.annotations.SerializedName;

public abstract class TrackItem
{
    private Date     time;
    @SerializedName("source")
    private int      sourceID;
    @SerializedName("vendor")
    private String   pluginName;
    @SerializedName("track")
    private String   trackName;
    @SerializedName("version")
    private int      trackVersion;
    
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