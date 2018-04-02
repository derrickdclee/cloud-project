package edu.duke.compsci290.partyappandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.User;

public class HostPartyActivity extends AppCompatActivity {

    private Party mParty;
    private ArrayList<User> mUsersFriends;
    private Button selectedButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_party);
        Intent intent = this.getIntent();
        mParty = (Party) intent.getSerializableExtra("party_object");
        mUsersFriends = (ArrayList<User>) intent.getSerializableExtra("user_friends");

        final Button toInviteButton = findViewById(R.id.to_invite_button);
        final Button invitedButton = findViewById(R.id.invited_button);
        final Button rsvpedButton = findViewById(R.id.rsvped_button);
        final Button checkedInButton = findViewById(R.id.checked_in_button);
        selectedButton = toInviteButton;
        toInviteButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        toInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFriendsDisplay(toInviteButton);
            }
        });

        invitedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFriendsDisplay(invitedButton);
            }
        });

        rsvpedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFriendsDisplay(rsvpedButton);
            }
        });

        checkedInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFriendsDisplay(checkedInButton);
            }
        });


        RecyclerView rv = findViewById(R.id.fb_friends_recycler_view);
        rv.setAdapter(new HostPartyAdapter(this, mUsersFriends, false));
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    private void selectFriendsDisplay(Button pressedButton){
        if (!selectedButton.equals(pressedButton)){
            pressedButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            selectedButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            selectedButton = pressedButton;
        }
    }
}
