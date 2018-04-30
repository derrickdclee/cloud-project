package edu.duke.compsci290.partyappandroid.EventPackage;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kennethkoch on 4/29/18.
 */

public class PartyInviteSpecifics extends PartyInvite {
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
