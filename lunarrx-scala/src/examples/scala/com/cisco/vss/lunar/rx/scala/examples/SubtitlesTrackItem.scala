package com.cisco.vss.lunar.rx.scala.examples

import com.cisco.vss.lunar.rx.plugin.core.TrackItem
import java.util.Date

class SubtitleLine(text: String){}

class SubtitlesTrackItem(
    sourceID:     Int, 
    time:         Date, 
    pluginName:   String, 
    trackName:    String, 
    trackVersion: Int,
    pst:          Long,
    lines:        Array[SubtitleLine]
    ) extends TrackItem(sourceID, time, pluginName, trackName, trackVersion) 
{
}