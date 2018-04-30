package edu.duke.compsci290.partyappandroid;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import edu.duke.compsci290.partyappandroid.EventPackage.Service;
import edu.duke.compsci290.partyappandroid.EventPackage.User;
import edu.duke.compsci290.partyappandroid.EventPackage.UserInvitation;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class PartyScanActivity extends AppCompatActivity {
    private NfcAdapter mNfcAdapter;
    private boolean mHasSentMessage;
    private TextView mNfcResultText;
    private TextView mUserNameText;
    private TextView mResultText;
    private ImageView mUserImage;
    private Service service;
    private ArrayList<UserInvitation> mUserInvitations;
    private Button mScanQrCode;
    private String mPartyId;
    private String accessToken;
    private CompositeDisposable compositeDisposable= new CompositeDisposable();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_scan);
        accessToken = "";
        SharedPreferences mPrefs = getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }
        //handleNfcIntent(getIntent());
        Intent intent = this.getIntent();
        mPartyId = intent.getStringExtra("party_id");
        mNfcResultText = findViewById(R.id.nfc_result_text);
        mUserNameText = findViewById(R.id.scan_user_name_text);
        mResultText = findViewById(R.id.scan_user_result_text);
        mUserImage = findViewById(R.id.scanned_user_facebook_picture);
        mScanQrCode = findViewById(R.id.scan_qr_code_button);
        mScanQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanQrCode();
            }
        });
        setupretrofit();
        getUserInvitations();


    }
    /*
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
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        /*
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK) {

                Log.d("Contents", result.getContents());
            }
            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        }*/
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {

            } else {
                Log.d("CONTENT", result.getContents());
                verifyUserId(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);

        }

    }


    @Override
    public void onNewIntent(Intent intent) {
        Log.d("ISTHISCALLED", "WTF");
        //handleNfcIntent(intent);
    }
    @Override
    public void onResume(){
        super.onResume();
        //handleNfcIntent(getIntent());
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
        //mUserInvitations

        for (int i=0;i<mUserInvitations.size();i++){
            Log.d("USERID", mUserInvitations.get(i).getId());
        }
        Stream<UserInvitation> invitedUserStream = mUserInvitations.stream().filter(t -> t.getId().equals(id));
        Optional<UserInvitation> invitedUserOptional = invitedUserStream.findAny();
        if (invitedUserOptional.isPresent()){
            UserInvitation invitedUser = invitedUserOptional.get();
            Picasso.get().load("http://graph.facebook.com/" + invitedUser.getInvitee().getFacebook_id() + "/picture?type=large").into(mUserImage);
            mUserNameText.setText(invitedUser.getInvitee().getFull_name());
            mResultText.setText("Checking into database...");
            Log.d("access token", accessToken);
            Log.d("id", id);

            service.checkinUser("Bearer "+accessToken, id).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d("RESPONSE CODE", response.code()+"");
                    Log.d("RESPONSE MESSAGE", response.message());
                    Log.d("ID PUT IN", id);
                    mResultText.setText("User checked in");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });

        }
        else{
            mUserNameText.setText("UNKNOWN");
            mResultText.setText("User not invited");

        }
        /*
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
        mResultText.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGreen));*/
    }

    private void setupretrofit(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        service = new Retrofit.Builder().baseUrl("http://party-app-dev.us-west-2.elasticbeanstalk.com").addCallAdapterFactory(RxJava2CallAdapterFactory.create()).addConverterFactory(GsonConverterFactory.create(gson)).build().create(Service.class);
    }

    private void getUserInvitations(){
        Disposable disposable = service.getUsersInvited("Bearer "+accessToken, mPartyId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    mUserInvitations = (ArrayList<UserInvitation>) t;
                });
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        compositeDisposable.clear();
    }

    private void scanQrCode(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan a QRcode");
        integrator.setOrientationLocked(false);
        integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

}
