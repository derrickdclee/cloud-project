package edu.duke.compsci290.partyappandroid.EventPackage;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by kennethkoch on 4/16/18.
 */

public class UserInvitation {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("invitee")
    @Expose
    private String invitee;
    @SerializedName("party")
    @Expose
    private String party;
    @SerializedName("facebook_id")
    @Expose
    private String facebook_id;
    @SerializedName("has_rsvped")
    @Expose
    private boolean has_rsvped;
    @SerializedName("has_checkedin")
    @Expose
    private boolean has_checkedin;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return id;
    }
    public void setInvitee(String invitee){
        this.invitee = invitee;
    }
    public String getInvitee(){
        return invitee;
    }
    public void setParty(String party){
        this.party = party;
    }
    public String getParty(){
        return party;
    }
    public void setFacebook_id(String facebook_id){
        this.facebook_id = facebook_id;
    }
    public String getFacebook_id(){
        return facebook_id;
    }
    public void setHas_rsvped(boolean has_rsvped){
        this.has_rsvped = has_rsvped;
    }
    public boolean getHas_rsvped(){
        return has_rsvped;
    }
    public void setHas_checkedin(boolean has_checkedin){
        this.has_checkedin = has_checkedin;
    }
    public boolean getHas_checkedin(){
        return has_checkedin;
    }
}
