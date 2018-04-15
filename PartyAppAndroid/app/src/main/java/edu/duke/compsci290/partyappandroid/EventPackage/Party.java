package edu.duke.compsci290.partyappandroid.EventPackage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by kennethkoch on 3/22/18.
 */

public class Party implements Serializable {
    private String mPartyName;
    private String mPartyDescription;
    private String mStartTime;
    private String mEndTime;
    private String mLocation;
    private ArrayList<User> mHost;
    private ArrayList<User> mInvitedTo;
    private ArrayList<User> mRSVP;
    private ArrayList<User> mCheckedIn;
    private ArrayList<User> mBouncer;
    private PartyType mPartyType;
    public Party(String partyName, String partyDescription, String location, String startTime, String endTime){
        mPartyName = partyName;
        mPartyDescription = partyDescription;
        mStartTime = startTime;
        mEndTime = endTime;
        mLocation = location;
        mHost = new ArrayList<>();
        mInvitedTo = new ArrayList<>();
        mRSVP = new ArrayList<>();
        mCheckedIn = new ArrayList<>();
        mBouncer = new ArrayList<>();
    }
    public void addHost(User user){
        mHost.add(user);
    }
    public void addInvitedTo(User user){
        mInvitedTo.add(user);
    }
    public void addRSVP(User user){
        mRSVP.add(user);
    }
    public void addCheckedIn(User user){
        mCheckedIn.add(user);
    }
    public void addBouncer(User user){
        mBouncer.add(user);
    }

    public String getPartyName(){
        return mPartyName;
    }
    public String getPartyDescription(){
        return mPartyDescription;
    }
    public ArrayList<User> getHosts(){
        return mHost;
    }
    public ArrayList<User> getInvitedTo(){
        return mInvitedTo;
    }
    public ArrayList<User> getRSVPS(){
        return mRSVP;
    }
    public ArrayList<User> getCheckedIn(){
        return mCheckedIn;
    }
    public ArrayList<User> getBouncers(){
        return mBouncer;
    }

    public PartyUserStatus getUserStatus(User user){
        if (mCheckedIn.contains(user)){
            return PartyUserStatus.CHECKEDIN;
        }
        if (mRSVP.contains(user)){
            return PartyUserStatus.RSVP;
        }
        if (mInvitedTo.contains(user)){
            return PartyUserStatus.INVITED;
        }
        return PartyUserStatus.UNINVITED;
    }
}
