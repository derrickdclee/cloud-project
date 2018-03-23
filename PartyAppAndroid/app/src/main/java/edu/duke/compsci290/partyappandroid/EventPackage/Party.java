package edu.duke.compsci290.partyappandroid.EventPackage;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by kennethkoch on 3/22/18.
 */

public class Party {
    private String mPartyName;
    private String mPartyDescription;
    private Date mStartTime;
    private Date mEndTime;
    private ArrayList<User> mHost;
    private ArrayList<User> mInvitedTo;
    private ArrayList<User> mRSVP;
    private ArrayList<User> mCheckedIn;
    private ArrayList<User> mBouncer;
    private PartyType mPartyType;
    public Party(String partyName, String partyDescription, Date startTime, Date endTime){
        mPartyName = partyName;
        mPartyDescription = partyDescription;
        mStartTime = startTime;
        mEndTime = endTime;
        mHost = new ArrayList<>();
        mInvitedTo = new ArrayList<>();
        mRSVP = new ArrayList<>();
        mCheckedIn = new ArrayList<>();
        mBouncer = new ArrayList<>();
    }
}
