package edu.duke.compsci290.partyappandroid;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.PartyInvite;

public class InviteePartyActivity extends AppCompatActivity {
    private PartyInvite mParty;
    private TextView mPartyName;
    private TextView mPartyDescription;
    private ImageView mPartyImage;
    private TextView mPartyLocation;
    private TextView mPartyStartTime;
    private TextView mPartyEndTime;
    private Button mGoToMaps;
    private Button mNotGoing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitee_party);
        Intent intent = this.getIntent();
        mParty = (PartyInvite) intent.getSerializableExtra("party_object");
        mPartyName = findViewById(R.id.activity_invitee_party_name_text);
        mPartyName.setText(mParty.getName());

        mPartyDescription = findViewById(R.id.activity_invitee_party_description);
        mPartyDescription.setText(mParty.getDescription());

        mPartyImage = findViewById(R.id.activity_invitee_party_image);
        Picasso.get().load(mParty.getImage()).into(mPartyImage);

        mPartyStartTime = findViewById(R.id.activity_invitee_party_start_time);
        mPartyStartTime.setText(mParty.getStart_time());

        mPartyEndTime = findViewById(R.id.activity_invitee_party_end_time);
        mPartyEndTime.setText(mParty.getEnd_time());

        mPartyLocation = findViewById(R.id.activity_invitee_party_location);
        String addressForMaps = getAddressFromLatLng(Double.parseDouble(mParty.getLat()), Double.parseDouble(mParty.getLng()));
        mPartyLocation.setText(addressForMaps);

        mGoToMaps = findViewById(R.id.activity_invitee_party_go_to_maps);
        mGoToMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMaps(addressForMaps, mParty.getLat(), mParty.getLng());
            }
        });

        mNotGoing = findViewById(R.id.activity_invitee_not_going_button);
        mNotGoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //setNotGoingToParty();
            }
        });

    }

    private String getAddressFromLatLng(double lat, double lng){
        Geocoder myg = new Geocoder(this);
        try {
            List<Address> myaddr = myg.getFromLocation(lat, lng, 1);
            String addr = "";
            for (int i=0;i<=myaddr.get(0).getMaxAddressLineIndex();i++){
                addr += myaddr.get(0).getAddressLine(i);
            }
            return addr;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

    }

    private void goToMaps(String address, String lat, String lng){
        if (address.equals("")){
            String newaddress = address.replace(" ", "+");
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+newaddress);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
        else {
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+ lat +","+ lng);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
    }

    /*
    private void setNotGoingToParty(){
        mParty.
    }*/
}
