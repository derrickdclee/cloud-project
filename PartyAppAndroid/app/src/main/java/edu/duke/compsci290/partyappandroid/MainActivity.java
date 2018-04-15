package edu.duke.compsci290.partyappandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.duke.compsci290.partyappandroid.EventPackage.User;

public class MainActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private User mUser;
    private RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        if (AccessToken.getCurrentAccessToken()!=null){
            handleFacebookAccessToken(AccessToken.getCurrentAccessToken());
        }


        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        // If you are using in a fragment, call loginButton.setFragment(this);
        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                boolean loggedIn = AccessToken.getCurrentAccessToken() == null;
                Log.d("CHECKFORLOGIN", AccessToken.getCurrentAccessToken()+"");
                if (AccessToken.getCurrentAccessToken()!=null){
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d("EXCEPTION", exception.toString());
            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (AccessToken.getCurrentAccessToken()!=null){
            handleFacebookAccessToken(AccessToken.getCurrentAccessToken());
        }
    }

    private void goToPartyModesAsUser(){
        Intent intent = new Intent(this, PartyModesActivity.class);
        this.startActivity(intent);

    }
    private void bypass(){
        Intent intent = new Intent(this, PartyModesActivity.class);
        intent.putExtra("user_object", (Serializable) mUser);
        this.startActivity(intent);
    }

    private void handleFacebookAccessToken(final AccessToken token){
        String url = "http://party-app-dev.us-west-2.elasticbeanstalk.com/auth/convert-token/";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        try {
                            JSONObject json = new JSONObject(response);
                            String accessToken = json.getString("access_token");
                            String refreshToken = json.getString("refresh_token");
                            Log.d("ACCESSTOKEN", accessToken);
                            Log.d("REFRESHTOKEN", refreshToken);
                            SharedPreferences myPreferences = getSharedPreferences("app_tokens", MODE_PRIVATE);
                            SharedPreferences.Editor editor = myPreferences.edit();
                            editor.putString("access_token", accessToken);
                            editor.putString("refresh_token", refreshToken);
                            editor.commit();

                            goToPartyModesAsUser();

                        } catch (JSONException e) {
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
                    }
                }
        ) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                Log.d("FBTOKEN", token.getToken());
                params.put("grant_type", "convert_token");
                params.put("client_id", BuildConfig.DjangoClientId);
                params.put("client_secret", BuildConfig.DjangoSecretKey);
                params.put("backend", "facebook");
                params.put("token", token.getToken());
                return params;
            }

        };
        queue.add(postRequest);

    }

}
