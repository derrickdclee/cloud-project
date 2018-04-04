package edu.duke.compsci290.partyappandroid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.nio.charset.Charset;

import edu.duke.compsci290.partyappandroid.EventPackage.User;

public class PartyScanActivity extends AppCompatActivity {
    private NfcAdapter mNfcAdapter;
    private boolean mHasSentMessage;
    private TextView mNfcResultText;
    private TextView mUserNameText;
    private TextView mResultText;
    private ImageView mUserImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_scan);
        //handleNfcIntent(getIntent());
        mNfcResultText = findViewById(R.id.nfc_result_text);
        mUserNameText = findViewById(R.id.scan_user_name_text);
        mResultText = findViewById(R.id.scan_user_result_text);
        mUserImage = findViewById(R.id.scanned_user_facebook_picture);

    }

    private void handleNfcIntent(Intent NfcIntent) {
        Log.d("NFCTESTING", "testin some nfc");

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(NfcIntent.getAction())) {
            Parcelable[] receivedArray =
                    NfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if(receivedArray != null) {

                NdefMessage receivedMessage = (NdefMessage) receivedArray[0];
                NdefRecord[] attachedRecords = receivedMessage.getRecords();
                for (NdefRecord record:attachedRecords) {
                    String receivedUserId = new String(record.getPayload());
                    //Make sure we don't pass along our AAR (Android Application Record)
                    if (receivedUserId.equals(getPackageName())) { continue; }
                    Log.d("NFCTESTING", receivedUserId);
                    verifyUserId(receivedUserId);
                    break;
                }
            }
            else {
                Toast.makeText(this, "Received Blank Parcel", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onNewIntent(Intent intent) {
        Log.d("ISTHISCALLED", "WTF");
        handleNfcIntent(intent);
    }
    @Override
    public void onResume(){
        super.onResume();
        handleNfcIntent(getIntent());
    }
    @Override
    public void onPause(){
        super.onPause();
        Log.d("ONPAUSE", "On pause called");
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            this.finish();
        }

    }

    private void verifyUserId(String id){
        //hit database


        //after success
        boolean recordIsEmpty = false;
        boolean userIsInvited = true;
        //test user
        String returnedId = id;//"abc";
        String returnedName = "Test Name";
        User exampleUser = new User(returnedId, returnedName);
        if (recordIsEmpty){
            mResultText.setText("Cannot Find User Record");
            mResultText.setBackgroundColor(ContextCompat.getColor(this, R.color.colorRed));
            return;
        }
        Picasso.get().load("http://graph.facebook.com/" + returnedId + "/picture?type=large").into(mUserImage);
        mUserNameText.setText(returnedName);
        if (!userIsInvited){
            mResultText.setText("User Is Not Invited!");
            mResultText.setBackgroundColor(ContextCompat.getColor(this, R.color.colorRed));
            return;
        }
        mResultText.setText("User Successfully Checked In");
        mResultText.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGreen));
    }
}
