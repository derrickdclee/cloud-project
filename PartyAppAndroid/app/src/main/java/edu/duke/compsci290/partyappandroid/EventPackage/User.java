package edu.duke.compsci290.partyappandroid.EventPackage;

import java.io.Serializable;

/**
 * Created by kennethkoch on 3/22/18.
 */

public class User implements Serializable {
    private String mId;
    private String mName;
    private String mEmail;

    public User(String id, String name){
        mId = id;
        mName = name;
    }
    public void setUserEmail(String email){
        mEmail = email;
    }
    public String getUserId(){
        return mId;
    }
    public String getUserEmail(){
        return mEmail;
    }
    public String getUserName(){
        return mName;
    }
}
