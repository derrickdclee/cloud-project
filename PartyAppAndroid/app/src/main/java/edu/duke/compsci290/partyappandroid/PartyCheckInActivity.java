package edu.duke.compsci290.partyappandroid;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.nio.charset.Charset;

public class PartyCheckInActivity extends AppCompatActivity implements NfcAdapter.OnNdefPushCompleteCallback, NfcAdapter.CreateNdefMessageCallback{

    private NfcAdapter mNfcAdapter;
    private boolean mHasSentMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_check_in);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mHasSentMessage = false;
        if(mNfcAdapter != null) {
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

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        Log.d("NFCSent", "Successfully sent");
    }
}
