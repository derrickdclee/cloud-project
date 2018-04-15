package edu.duke.compsci290.partyappandroid;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;

import java.nio.charset.Charset;

import bolts.Task;
import edu.duke.compsci290.partyappandroid.EventPackage.User;

public class PartyCheckInActivity extends AppCompatActivity implements NfcAdapter.OnNdefPushCompleteCallback, NfcAdapter.CreateNdefMessageCallback{

    private NfcAdapter mNfcAdapter;
    private boolean mHasSentMessage;
    private User mUser;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_check_in);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mHasSentMessage = false;
        Intent intent = this.getIntent();
        mUser = (User) intent.getSerializableExtra("user_object");
        if(mNfcAdapter != null) {
            //This will refer back to createNdefMessage for what it will send
            mNfcAdapter.setNdefPushMessageCallback(this, this);

            //This will be called if the message is sent successfully
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
        
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        if (mHasSentMessage){
            return null;
        }
        //We'll write the createRecords() method in just a moment
        //NdefRecord[] recordsToSend = new NdefRecord[1];
        //recordsToSend[0] = createRecord();
        //NdefRecord recordToSend = createRecord();
        //When creating an NdefMessage we need to provide an NdefRecord[]
        NdefRecord[] recordsToSend = createRecords();
        return new NdefMessage(recordsToSend);
    }

    public NdefRecord[] createRecords() {
        NdefRecord[] records = new NdefRecord[2];
        //To Create Messages Manually if API is less than
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            String messageToSend = mUser.getUserId();
            byte[] payload = messageToSend.
                    getBytes(Charset.forName("UTF-8"));
            NdefRecord record = new NdefRecord(
                    NdefRecord.TNF_WELL_KNOWN,      //Our 3-bit Type name format
                    NdefRecord.RTD_TEXT,            //Description of our payload
                    new byte[0],                    //The optional id for our Record
                    payload);                       //Our payload for the Record
            records[0] = record;
        }
        //Api is high enough that we can use createMime, which is preferred.
        else {

            String messageToSend = mUser.getUserId();
            byte[] payload = messageToSend.
                    getBytes(Charset.forName("UTF-8"));

            NdefRecord record = NdefRecord.createMime("text/plain",payload);
            records[0] = record;
        }
        records[1] =
                NdefRecord.createApplicationRecord(getPackageName());
        return records;
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        Log.d("NFCSent", "Successfully sent");
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

    private void verifyUserLocation(){
        try {
            //if (mLocationPermissionGranted) {
                final com.google.android.gms.tasks.Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.

                            Location lastKnownLocation = (Location) task.getResult();
                            Log.d("LONGITUDE", lastKnownLocation.getLongitude()+"");
                            Log.d("LATITUDE", lastKnownLocation.getLatitude()+"");
                            Log.d("ACCURACY", lastKnownLocation.getAccuracy()+"");
                            Log.d("TIME", lastKnownLocation.getTime()+"");


                        } else {
                            Log.d("DEBUG", "Current location is null. Using defaults.");
                            Log.e("DEBUG", "Exception: %s", task.getException());
                        }
                    }
                });
            //}
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}
