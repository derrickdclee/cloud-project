package edu.duke.compsci290.partyappandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.User;

public class HostPartyActivity extends AppCompatActivity {

    private Party mParty;
    private ArrayList<User> mUsersFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_party);
        Intent intent = this.getIntent();
        mParty = (Party) intent.getSerializableExtra("party_object");
        mUsersFriends = (ArrayList<User>) intent.getSerializableExtra("user_friends");
        ArrayList<String> friendsList = new ArrayList<>();
        friendsList.add("Michelle Wolf");
        friendsList.add("Random Ass Person");
        RecyclerView rv = findViewById(R.id.fb_friends_recycler_view);
        rv.setAdapter(new HostPartyAdapter(this, mUsersFriends, false));
        rv.setLayoutManager(new LinearLayoutManager(this));
    }
}
