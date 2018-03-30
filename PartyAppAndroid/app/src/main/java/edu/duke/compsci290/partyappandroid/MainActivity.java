package edu.duke.compsci290.partyappandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;

import edu.duke.compsci290.partyappandroid.EventPackage.User;

public class MainActivity extends AppCompatActivity {
    private static final String EMAIL = "email";
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private User mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (AccessToken.getCurrentAccessToken()!=null){
            goToPartyModesAsUser();
        }
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));
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
                goToPartyModesAsUser();
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

        Button bypassbutton = findViewById(R.id.bypass_button);
        bypassbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bypass();
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
            goToPartyModesAsUser();
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

}
