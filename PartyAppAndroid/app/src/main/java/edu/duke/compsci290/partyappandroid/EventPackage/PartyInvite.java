package edu.duke.compsci290.partyappandroid.EventPackage;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by kennethkoch on 4/20/18.
 */

public class PartyInvite implements Serializable{
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("host")
    @Expose
    private DjangoUser host;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("lat")
    @Expose
    private String lat;

    @SerializedName("lng")
    @Expose
    private String lng;

    @SerializedName("start_time")
    @Expose
    private String start_time;

    @SerializedName("end_time")
    @Expose
    private String end_time;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return id;
    }

    public void setHost(DjangoUser host){
        this.host = host;
    }
    public DjangoUser getHost(){
        return host;
    }

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

    public void setDescription(String description){
        this.description = description;
    }
    public String getDescription(){
        return description;
    }

    public void setImage(String image){
        this.image = image;
    }
    public String getImage(){
        return image;
    }

    public void setLat(String lat){
        this.lat = lat;
    }
    public String getLat(){
        return lat;
    }

    public void setLng(String lng){
        this.lng = lng;
    }
    public String getLng(){
        return lng;
    }

    public void setStart_time(String start_time){
        this.start_time = start_time;
    }
    public String getStart_time(){
        return start_time;
    }

    public void setEnd_time(String end_time){
        this.end_time = end_time;
    }
    public String getEnd_time(){
        return end_time;
    }


    @SerializedName("bouncers")
    @Expose
    private List<DjangoUser> bouncers;

    public void setBouncers(List<DjangoUser> bouncers){
        this.bouncers = bouncers;
    }
    public List<DjangoUser> getBouncers(){
        return bouncers;
    }

    public void removeBouncer(DjangoUser bouncer){
        bouncers.remove(bouncer);
    }
    public void addBouncer(DjangoUser bouncer){
        bouncers.add(bouncer);
    }

}
