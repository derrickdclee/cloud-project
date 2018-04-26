package edu.duke.compsci290.partyappandroid.SpotifyPackage;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by kennethkoch on 4/26/18.
 */

public class SpotifyResponse {
    @SerializedName("tracks")
    @Expose
    private TrackInfo tracks;
    public void setTracks(TrackInfo tracks){
        this.tracks = tracks;
    }
    public TrackInfo getTracks(){
        return tracks;
    }
}
