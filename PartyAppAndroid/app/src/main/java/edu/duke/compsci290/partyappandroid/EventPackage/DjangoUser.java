package edu.duke.compsci290.partyappandroid.EventPackage;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by kennethkoch on 4/22/18.
 */

public class DjangoUser implements Serializable{
    @SerializedName("full_name")
    @Expose
    private String full_name;
    @SerializedName("facebook_id")
    @Expose
    private String facebook_id;
    public void setFull_name(String full_name){
        this.full_name = full_name;
    }
    public String getFull_name(){
        return full_name;
    }
    public void setFacebook_id(String facebook_id){
        this.facebook_id = facebook_id;
    }
    public String getFacebook_id(){
        return facebook_id;
    }
}
