package edu.duke.compsci290.partyappandroid;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.User;

public class HostActivity extends AppCompatActivity {
    private Button mNewPartyButton;
    private ArrayList<Party> mPartiesHosting;
    private ArrayList<User> mUsersFriends;
    private HostAdapter mHostAdapter;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        queue = Volley.newRequestQueue(this);

        mNewPartyButton = findViewById(R.id.new_party_button);
        mNewPartyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newPartyActivity();
            }
        });
        getUserParties();


        Date startDate = new Date();
        Party testParty = new Party("Test Party", "This party is a test party. It is going to be a rager",
                "location", startDate.toString(), startDate.toString());
        mPartiesHosting = new ArrayList<>();
        mUsersFriends = new ArrayList<>();
        mPartiesHosting.add(testParty);
        RecyclerView rv = findViewById(R.id.parties_host_recycler_view);
        mHostAdapter = new HostAdapter(this, mPartiesHosting);
        rv.setAdapter(mHostAdapter);
        //rv.setAdapter(new HostAdapter(this, mPartiesHosting));
        rv.setLayoutManager(new LinearLayoutManager(this));
        /*
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        String id = "";
                        try{
                            id = object.getString("id");
                            String email = object.getString("email");
                            String name = object.getString("name");
                        } catch (JSONException e){
                            e.printStackTrace();
                        }

                        getUserFriends(id);
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();*/

    }
    private void newPartyActivity(){
        Intent intent = new Intent(this, AddPartyActivity.class);
        this.startActivity(intent);
    }

    /*
    private void getUserFriends(String userId){
        final GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + userId + "/friends",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONObject jsonResponse = response.getJSONObject();
                        try{
                            JSONArray friendArray = jsonResponse.getJSONArray("data");
                            for (int i=0; i<friendArray.length();i++){
                                JSONObject friend = friendArray.getJSONObject(i);
                                User friendUser;
                                String friendName = friend.getString("name");
                                String friendId = friend.getString("id");
                                friendUser = new User(friendId, friendName);
                                Log.d("FRIENDNAME", friendUser.getUserName());
                                Log.d("FRIENDID", friendUser.getUserId());
                                mUsersFriends.add(friendUser);
                            }
                            mHostAdapter.setUserFriends(mUsersFriends);
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                });

        request.executeAsync();
    }*/

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
