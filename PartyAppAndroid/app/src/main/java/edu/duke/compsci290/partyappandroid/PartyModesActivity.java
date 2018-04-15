package edu.duke.compsci290.partyappandroid;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONException;
import org.json.JSONObject;

import edu.duke.compsci290.partyappandroid.EventPackage.User;

public class PartyModesActivity extends AppCompatActivity {
    private Button mHostButton;
    private Button mBouncerButton;
    private Button mInviteeButton;
    private User mUser;
    private boolean mLocationPermissionGranted;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        adapter.setNdefPushMessage(null, this, this);
        setContentView(R.layout.activity_party_modes);
        mHostButton = findViewById(R.id.host_mode_button);
        mBouncerButton = findViewById(R.id.bouncer_mode_button);
        mInviteeButton = findViewById(R.id.invitee_mode_button);
        mHostButton.setText("Host Mode");
        mHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToHostMode();
            }
        });
        mInviteeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToInviteeMode();
            }
        });
        mBouncerButton.setText("Bouncer Mode");
        mInviteeButton.setText("Invitee Mode");
        getLocationPermission();


        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        String id = "";
                        String name = "";
                        String email = "";
                        try{
                            id = object.getString("id");
                            email = object.getString("email");
                            name = object.getString("name");
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                        mUser = new User(id, name);
                        mUser.setUserEmail(email);

                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }
    private void goToHostMode(){
        Intent intent = new Intent(this, HostActivity.class);
        this.startActivity(intent);
    }
    private void goToInviteeMode(){
        if (!mLocationPermissionGranted){
            getLocationPermission();
            return;
        }
        Intent intent = new Intent(this, PartyCheckInActivity.class);
        if (mUser != null){
            intent.putExtra("user_object", mUser);
            this.startActivity(intent);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }
    @Override
    public void onPause() {
        super.onPause();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
    }
    @Override
    public void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            // drop NFC events

        }
    }

    private void getLocationPermission(){
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            }
        }
    }
}
