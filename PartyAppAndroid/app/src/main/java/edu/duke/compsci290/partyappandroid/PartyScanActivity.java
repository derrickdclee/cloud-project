package edu.duke.compsci290.partyappandroid;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.nio.charset.Charset;

public class PartyScanActivity extends AppCompatActivity implements NfcAdapter.OnNdefPushCompleteCallback, NfcAdapter.CreateNdefMessageCallback {
    private NfcAdapter mNfcAdapter;
    private boolean mHasSentMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_scan);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mHasSentMessage = false;
        if(mNfcAdapter != null) {
            Log.d("GOODSIGN", "this is a good sign");
            //This will refer back to createNdefMessage for what it will send
            mNfcAdapter.setNdefPushMessageCallback(this, this);

            //This will be called if the message is sent successfully
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
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

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        Toast.makeText(this, "success!", Toast.LENGTH_LONG);
    }


    /*
    public NdefRecord createRecord() {
        String messageToSend = "This is a test message";
        byte[] payload = messageToSend.
                getBytes(Charset.forName("UTF-8"));

        NdefRecord record = new NdefRecord(
                NdefRecord.TNF_WELL_KNOWN,  //Our 3-bit Type name format
                NdefRecord.RTD_TEXT,        //Description of our payload
                new byte[0],                //The optional id for our Record
                payload);                   //Our payload for the Record
        return record;
    }*/



    public NdefRecord[] createRecords() {
        NdefRecord[] records = new NdefRecord[2];
        //To Create Messages Manually if API is less than
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            String messageToSend = "This is a test message";
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

            String messageToSend = "This is a test message";
            byte[] payload = messageToSend.
                    getBytes(Charset.forName("UTF-8"));

            NdefRecord record = NdefRecord.createMime("text/plain",payload);
            records[0] = record;
        }
        records[1] =
                NdefRecord.createApplicationRecord(getPackageName());
        return records;
    }



    private void handleNfcIntent(Intent NfcIntent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(NfcIntent.getAction())) {
            Parcelable[] receivedArray =
                    NfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if(receivedArray != null) {

                NdefMessage receivedMessage = (NdefMessage) receivedArray[0];
                NdefRecord[] attachedRecords = receivedMessage.getRecords();
                for (NdefRecord record:attachedRecords) {
                    String string = new String(record.getPayload());
                    //Make sure we don't pass along our AAR (Android Application Record)
                    if (string.equals(getPackageName())) { continue; }
                    Toast.makeText(this, "Received "+string, Toast.LENGTH_LONG);
                }
            }
            else {
                Toast.makeText(this, "Received Blank Parcel", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onNewIntent(Intent intent) {
        handleNfcIntent(intent);
    }
}
