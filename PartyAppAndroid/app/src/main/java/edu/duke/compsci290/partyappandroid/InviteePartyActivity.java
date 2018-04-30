package edu.duke.compsci290.partyappandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.PartyInvite;
import edu.duke.compsci290.partyappandroid.EventPackage.Service;
import edu.duke.compsci290.partyappandroid.EventPackage.UserInvitation;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

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
    private LinearLayout mLinearLayout;
    private TextView mRsvpText;
    private Button mGoingButton;
    private UserInvitation mUserInvitation;
    private Service service;
    private ImageView mQrImage;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final String QR_URL_BEGINNING = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitee_party);
        setupretrofit();
        Intent intent = this.getIntent();
        mParty = (PartyInvite) intent.getSerializableExtra("party_object");
        mUserInvitation = (UserInvitation) intent.getSerializableExtra("user_invitation");
        mPartyName = findViewById(R.id.activity_invitee_party_name_text);
        mPartyName.setText(mParty.getName());

        mPartyDescription = findViewById(R.id.activity_invitee_party_description);
        mPartyDescription.setText("Description: "+mParty.getDescription());

        mPartyImage = findViewById(R.id.activity_invitee_party_image);
        Picasso.get().load(mParty.getImage()).into(mPartyImage);

        SimpleDateFormat sdfForDjango = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssXXX", Locale.getDefault());
        SimpleDateFormat betterDateFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        String startTime = "Start Time: ";
        String endTime = "End Time: ";
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdfForDjango.parse(mParty.getStart_time()));
            startTime += betterDateFormat.format(c.getTime());
            c.setTime(sdfForDjango.parse(mParty.getEnd_time()));
            endTime += betterDateFormat.format(c.getTime());
        } catch (ParseException e){
            Log.d("PARSEECXEPTION", e.toString());
        }

        mPartyStartTime = findViewById(R.id.activity_invitee_party_start_time);
        mPartyStartTime.setText(startTime);

        mPartyEndTime = findViewById(R.id.activity_invitee_party_end_time);
        mPartyEndTime.setText(endTime);

        mPartyLocation = findViewById(R.id.activity_invitee_party_location);
        String addressForMaps = getAddressFromLatLng(Double.parseDouble(mParty.getLat()), Double.parseDouble(mParty.getLng()));
        mPartyLocation.setText("Location: "+addressForMaps);

        mGoToMaps = findViewById(R.id.activity_invitee_party_go_to_maps);
        mGoToMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMaps(addressForMaps, mParty.getLat(), mParty.getLng());
            }
        });


        mLinearLayout = findViewById(R.id.activity_invitee_parent_linear_layout);
        mRsvpText = findViewById(R.id.activity_invitee_party_rsvp_text);
        mQrImage = findViewById(R.id.activity_invitee_party_qr);

        mNotGoing = findViewById(R.id.activity_invitee_not_going_button);
        mNotGoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNotGoingToParty();
            }
        });

        mGoingButton = findViewById(R.id.activity_invitee_is_going_button);
        mGoingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setGoingToParty();
            }
        });
        if (mUserInvitation.getHas_rsvped()){
            mRsvpText.setVisibility(View.INVISIBLE);
            mGoingButton.setVisibility(View.INVISIBLE);
            mNotGoing.setVisibility(View.INVISIBLE);
        }

        Picasso.get().load(QR_URL_BEGINNING+mUserInvitation.getId()).into(mQrImage);
        //Log.d("PARTYID", mParty.getId());


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

    private void setupretrofit(){
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        service = new Retrofit.Builder().baseUrl("http://party-app-dev.us-west-2.elasticbeanstalk.com").client(clientBuilder.build()).addCallAdapterFactory(RxJava2CallAdapterFactory.create()).addConverterFactory(GsonConverterFactory.create(gson)).build().create(Service.class);
    }
    /*
    private Single<UserInvitation> getUserInfo(){
        String accessToken = "";
        SharedPreferences mPrefs = getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }
        return service.getMyInvitation("Bearer "+accessToken, mParty.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }*/


    private void setNotGoingToParty(){
        String accessToken = "";
        SharedPreferences mPrefs = getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }
        service.removeInvitee("Bearer "+accessToken, mUserInvitation.getId())
                .enqueue(new Callback<Response<Void>>() {
                    @Override
                    public void onResponse(Call<Response<Void>> call, Response<Response<Void>> response) {
                        finish();
                    }

                    @Override
                    public void onFailure(Call<Response<Void>> call, Throwable t) {

                    }
                });
    }

    private void setGoingToParty(){
        Log.d("DOES THIS HIT", "maybe");
        String accessToken = "";
        SharedPreferences mPrefs = getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }
        Log.d("access token", accessToken);



        service.rsvpUser("Bearer "+accessToken, mUserInvitation.getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.d("response", response+"");
                        if (response.code()==200){
                            mRsvpText.setVisibility(View.INVISIBLE);
                            mGoingButton.setVisibility(View.INVISIBLE);
                            mNotGoing.setVisibility(View.INVISIBLE);
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        compositeDisposable.clear();
    }
}
