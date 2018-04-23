package edu.duke.compsci290.partyappandroid;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.PartyInvite;
import edu.duke.compsci290.partyappandroid.EventPackage.Service;
import edu.duke.compsci290.partyappandroid.EventPackage.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HostActivity extends AppCompatActivity {
    private Button mNewPartyButton;
    private ArrayList<Party> mPartiesHosting;
    private ArrayList<User> mUsersFriends;
    private HostAdapter mHostAdapter;
    private RecyclerView rv;
    private RequestQueue queue;
    private Service service;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        queue = Volley.newRequestQueue(this);
        setupretrofit();
        mNewPartyButton = findViewById(R.id.new_party_button);
        mNewPartyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newPartyActivity();
            }
        });
        getUserParties2();
        mPartiesHosting = new ArrayList<>();
        mUsersFriends = new ArrayList<>();
        rv = findViewById(R.id.parties_host_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));

    }
    private void newPartyActivity(){
        Intent intent = new Intent(this, AddPartyActivity.class);
        this.startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        //nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }
    @Override
    public void onPause() {
        super.onPause();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //nfcAdapter.disableForegroundDispatch(this);
    }
    @Override
    public void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            // drop NFC events

        }
    }


    private void setupretrofit(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        service = new Retrofit.Builder().baseUrl("http://party-app-dev.us-west-2.elasticbeanstalk.com").addCallAdapterFactory(RxJava2CallAdapterFactory.create()).addConverterFactory(GsonConverterFactory.create(gson)).build().create(Service.class);
    }


    private void getUserParties2(){
        String accessToken = "";
        SharedPreferences mPrefs = getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }
        Disposable toDispose = service.getPartiesHosting("Bearer "+accessToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    rv.setAdapter(new RedoneHostAdapter(getApplicationContext(), (ArrayList<PartyInvite>) t));
                });
        compositeDisposable.add(toDispose);
    }




    private void getUserParties(){
        String accessToken = "";
        SharedPreferences mPrefs = getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }

        Log.d("ACCESS_TOKEN", accessToken);
        String url = "http://party-app-dev.us-west-2.elasticbeanstalk.com/parties/hosted/me";
        final String finalAccessToken = accessToken;
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i=0;i<jsonArray.length();i++){
                                JSONObject json = jsonArray.getJSONObject(i);
                                String name = json.getString("name");
                                String description = json.getString("description");
                                String imageUrl = json.getString("image");
                                String latString = json.getString("lat");
                                String lngString = json.getString("lng");
                                String startTime = json.getString("start_time");
                                String endTime = json.getString("end_time");
                                String id = json.getString("id");
                                double lat = Double.parseDouble(latString);
                                double lng = Double.parseDouble(lngString);

                                Geocoder geocoder;
                                List<Address> addresses;
                                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                                addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                String fullAddress = "";

                                for (int j=0;j<=addresses.get(0).getMaxAddressLineIndex();j++){
                                    fullAddress += addresses.get(0).getAddressLine(j);
                                }
                                /*
                                String address = addresses.get(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                String city = addresses.get(0).getLocality();
                                String state = addresses.get(0).getAdminArea();*/
                                /*
                                String country = addresses.get(0).getCountryName();
                                String postalCode = addresses.get(0).getPostalCode();
                                String knownName = addresses.get(0).getFeatureName();*/
                                Party newParty = new Party(name, description, fullAddress, startTime, endTime);
                                newParty.setImageUri(imageUrl);
                                newParty.setPartyId(id);

                                mPartiesHosting.add(newParty);
                            }
                            rv.setAdapter(new HostAdapter(getApplicationContext(), mPartiesHosting));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                        Log.d("ACCESS_TOKEN", "whatever");
                    }
                }
        )

        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + finalAccessToken);
                return params;
            }
        };
        queue.add(postRequest);

    }

}
