package edu.duke.compsci290.partyappandroid.SpotifyPackage;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by kennethkoch on 4/26/18.
 */

public class SpotifySearchItem {
    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("uri")
    @Expose
    private String uri;

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

    public void setUri(String uri){
        this.uri = uri;
    }
    public String getUri(){
        return uri;
    }
}
