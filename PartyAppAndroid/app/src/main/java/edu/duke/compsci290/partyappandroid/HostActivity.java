package edu.duke.compsci290.partyappandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
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

import edu.duke.compsci290.partyappandroid.EventPackage.MyDeletePartyListener;
import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.PartyInvite;
import edu.duke.compsci290.partyappandroid.EventPackage.Service;
import edu.duke.compsci290.partyappandroid.EventPackage.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HostActivity extends AppCompatActivity implements MyDeletePartyListener{
    private Button mNewPartyButton;
    private RecyclerView rv;
    private Service service;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        setupretrofit();
        mNewPartyButton = findViewById(R.id.new_party_button);
        mNewPartyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newPartyActivity();
            }
        });
        getUserParties();
        rv = findViewById(R.id.parties_host_recycler_view);
        Log.d("DEBUG", "WHYS this not getting called");
        rv.setLayoutManager(new LinearLayoutManager(this));


    }
    private void newPartyActivity(){
        Intent intent = new Intent(this, AddPartyActivity.class);
        this.startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserParties();

        /*
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);*/
    }
    @Override
    public void onPause() {
        super.onPause();
        /*
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);*/
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


    private void getUserParties(){
        String accessToken = "";
        SharedPreferences mPrefs = getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }
        Log.d("access token", accessToken);
        Disposable toDispose = service.getPartiesHosting("Bearer "+accessToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    rv.setAdapter(new HostAdapter(this, (ArrayList<PartyInvite>) t));
                });
        compositeDisposable.add(toDispose);
    }

    @Override
    public void callback(String pid) {
        String accessToken = "";
        SharedPreferences mPrefs = getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }
        DeletePartyFragment deletePartyFragment = new DeletePartyFragment();
        deletePartyFragment.setPartyId(pid);
        deletePartyFragment.setAuthToken(accessToken);
        deletePartyFragment.setContext(this);
        deletePartyFragment.setDeleteService(service);
        deletePartyFragment.show(getFragmentManager(), "DeletePartyFragment");
    }

    public static class DeletePartyFragment extends DialogFragment {
        private String partyId;
        private Service deleteService;
        private String authToken;
        private Context mcontext;
        public void setPartyId(String partyId){
            this.partyId = partyId;
        }
        public void setAuthToken(String authtoken){
            authToken = authtoken;
        }
        public void setDeleteService(Service deleteService){
            this.deleteService = deleteService;
        }
        public void setContext(Context context){
            mcontext = context;
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete Party?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d("DELETE", partyId);
                            deleteService.deleteParty("Bearer "+authToken,
                                    partyId).enqueue(new Callback<retrofit2.Response<Void>>() {
                                @Override
                                public void onResponse(Call<retrofit2.Response<Void>> call, retrofit2.Response<retrofit2.Response<Void>> response) {
                                    ((HostActivity)mcontext).getUserParties();
                                }

                                @Override
                                public void onFailure(Call<retrofit2.Response<Void>> call, Throwable t) {

                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            Log.d("CANCEL", partyId);
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

}
