package edu.duke.compsci290.partyappandroid.SpotifyPackage;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by kennethkoch on 4/26/18.
 */

public class TrackInfo {
    @SerializedName("items")
    @Expose
    private List<SpotifySearchItem> items;

    public void setItems(List<SpotifySearchItem> items){
        this.items = items;
    }
    public List<SpotifySearchItem> getItems(){
        return items;
    }
}
