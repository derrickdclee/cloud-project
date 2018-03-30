package edu.duke.compsci290.partyappandroid.EventPackage;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by kennethkoch on 3/22/18.
 */

public class AllUserInfo {
    private User mCurrentUser;
    private ArrayList<Party> mPartiesHosting;
    private ArrayList<Party> mPartiesBouncing;
    private ArrayList<Party> mPartiesInvited;
    public AllUserInfo(User currentUser){
        mCurrentUser = currentUser;
        mPartiesHosting = new ArrayList<>();
        mPartiesBouncing = new ArrayList<>();
        mPartiesInvited = new ArrayList<>();
    }
    public void addPartyHosting(Party party){
        mPartiesHosting.add(party);
    }
    public void addPartyBouncing(Party party){
        mPartiesBouncing.add(party);
    }
    public void addPartyInvited(Party party){
        mPartiesInvited.add(party);
    }
    public ArrayList<Party> getPartiesHosting(){
        return mPartiesHosting;
    }
    public ArrayList<Party> getPartiesBouncing(){
        return mPartiesBouncing;
    }
    public ArrayList<Party> getPartiesInvited(){
        return mPartiesInvited;
    }

}
