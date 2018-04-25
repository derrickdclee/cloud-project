package edu.duke.compsci290.partyappandroid.EventPackage;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by kennethkoch on 4/22/18.
 */

public class DjangoUser implements Serializable{
    @SerializedName("id")
    @Expose
    private String id;
    /*
    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("email")
    @Expose
    private String email;
    */
    @SerializedName("full_name")
    @Expose
    private String full_name;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return id;
    }
    /*
    public void setUsername(String username){
        this.username = username;
    }
    public String getUsername(){
        return username;
    }

    public void setEmail(String email){
        this.email = email;
    }
    public String getEmail(){
        return email;
    }
    */
    public void setFull_name(String full_name){
        this.full_name = full_name;
    }
    public String getFull_name(){
        return full_name;
    }

}
