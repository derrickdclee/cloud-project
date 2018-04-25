package edu.duke.compsci290.partyappandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.u.rxfacebook.RxFacebook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.duke.compsci290.partyappandroid.EventPackage.FacebookUser;
import edu.duke.compsci290.partyappandroid.EventPackage.InviteFilterStatus;
import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.PartyInvite;
import edu.duke.compsci290.partyappandroid.EventPackage.Service;
import edu.duke.compsci290.partyappandroid.EventPackage.User;
import edu.duke.compsci290.partyappandroid.EventPackage.UserInvitation;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class HostPartyActivity extends AppCompatActivity {

    private PartyInvite mParty;
    private ArrayList<User> mUsersFriends;
    private ArrayList<User> mFriendsToInvite;

    private Button selectedButton;
    private RecyclerView rv;
    private Service service;
    private ArrayList<UserInvitation> mUserInvitationList;


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_party);
        Intent intent = this.getIntent();
        mUsersFriends = new ArrayList<>();
        mFriendsToInvite = new ArrayList<>();
        setupretrofit();
        mParty = (PartyInvite) intent.getSerializableExtra("party_object");


        final Button toInviteButton = findViewById(R.id.to_invite_button);
        final Button invitedButton = findViewById(R.id.invited_button);
        final Button rsvpedButton = findViewById(R.id.rsvped_button);
        final Button checkedInButton = findViewById(R.id.checked_in_button);
        final Button bouncerButton = findViewById(R.id.bouncers_button);
        selectedButton = toInviteButton;
        toInviteButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        toInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFriendsDisplay(toInviteButton);
            }
        });

        invitedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFriendsDisplay(invitedButton);
            }
        });

        rsvpedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFriendsDisplay(rsvpedButton);
            }
        });

        checkedInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFriendsDisplay(checkedInButton);
            }
        });

        bouncerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFriendsDisplay(bouncerButton);
            }
        });

        rv = findViewById(R.id.fb_friends_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new HostPartyPotentialInviteeListAdapter(null, null, null));


        Disposable serviceCall = executeRx2java().subscribe(t->{
            Log.d("IS THIS CALLED", "WELL IS IT");
            List<FacebookUser> friendsToInvite = t.fbUsers;
            Set<String> invitedIds = new HashSet<>();
            for (int i=0;i<t.uInvitations.size();i++){
                invitedIds.add(t.uInvitations.get(i).getFacebook_id());
            }
            for(Iterator<FacebookUser> iterator = friendsToInvite.iterator(); iterator.hasNext(); ) {
                if(invitedIds.contains(iterator.next().getId())){
                    iterator.remove();
                }
            }
            rv.setAdapter(new HostPartyPotentialInviteeListAdapter(this, (ArrayList<FacebookUser>) friendsToInvite, mParty));
        });
        compositeDisposable.add(serviceCall);
        //randomtest();
    }


    private void selectFriendsDisplay(Button pressedButton){
        if (!selectedButton.equals(pressedButton)){
            pressedButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            selectedButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            selectedButton = pressedButton;
            refreshData();
        }
    }

    private void refreshData(){
        Disposable serviceCall = executeRx2java().subscribe(t->{
            List<FacebookUser> friendsToInvite = t.fbUsers;
            List<FacebookUser> invited = new ArrayList<>();
            for (int i=0;i<t.uInvitations.size();i++){
                FacebookUser newlyInvited = new FacebookUser();
                newlyInvited.setId(t.uInvitations.get(i).getFacebook_id());
                newlyInvited.setName(t.uInvitations.get(i).getInvitee().getFull_name());
                invited.add(newlyInvited);
            }
            Set<String> invitedIds = new HashSet<>();
            for (int i=0;i<t.uInvitations.size();i++){
                invitedIds.add(t.uInvitations.get(i).getFacebook_id());
            }
            for(Iterator<FacebookUser> iterator = friendsToInvite.iterator(); iterator.hasNext(); ) {
                if(invitedIds.contains(iterator.next().getId())){
                    iterator.remove();
                }
            }

            switch (selectedButton.getId()) {
                case R.id.to_invite_button:
                    rv.setAdapter(new HostPartyPotentialInviteeListAdapter(this, (ArrayList<FacebookUser>) friendsToInvite, mParty));
                    break;
                case R.id.invited_button:
                    rv.setAdapter(new HostPartyInvitedListAdapter(this, (ArrayList<UserInvitation>) t.uInvitations, mParty, InviteFilterStatus.INVNITED));
                    break;
                case R.id.rsvped_button:
                    List<UserInvitation> rsvped = new ArrayList<>(t.uInvitations);
                    rsvped.removeIf(user-> !user.getHas_rsvped());
                    rv.setAdapter(new HostPartyInvitedListAdapter(this, (ArrayList<UserInvitation>)rsvped, mParty, InviteFilterStatus.RSVP));
                    break;
                case R.id.checked_in_button:
                    List<UserInvitation> checkedin = new ArrayList<>(t.uInvitations);
                    checkedin.removeIf(user-> !user.getHas_rsvped());
                    rv.setAdapter(new HostPartyInvitedListAdapter(this, (ArrayList<UserInvitation>)checkedin, mParty, InviteFilterStatus.CHECKEDIN));
                    break;
                case R.id.bouncers_button:
            }

        });
        compositeDisposable.add(serviceCall);

    }

    private void setupretrofit(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        service = new Retrofit.Builder().baseUrl("http://party-app-dev.us-west-2.elasticbeanstalk.com").addCallAdapterFactory(RxJava2CallAdapterFactory.create()).addConverterFactory(GsonConverterFactory.create(gson)).build().create(Service.class);
    }



    private Single<FacebookAndInvitation> executeRx2java(){
        String accessToken = "";
        String facebook_id = "";
        SharedPreferences mPrefs = getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }
        if (mPrefs.contains("facebook_id")){
            facebook_id = mPrefs.getString("facebook_id", "");
        }

        Log.d("facebookid", facebook_id);
        Log.d("partyid", mParty.getId());
        Log.d("accesstoken", accessToken);

        Gson gson = new Gson();
        Single<List<UserInvitation>> invitationsObserver = service.getUsersInvited("Bearer "+accessToken, mParty.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + facebook_id + "/friends");


        Single<List<FacebookUser>> friendsObserver = Single.defer(()->Single.just(request.executeAndWait()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(t -> {
                    Log.d("WTF", t.toString());
                    ArrayList<FacebookUser> friends = gson.fromJson(t.getJSONObject().getJSONArray("data").toString(), new TypeToken<ArrayList<FacebookUser>>(){}.getType());
                    return friends;
                });

       return Single.zip(invitationsObserver, friendsObserver, new BiFunction<List<UserInvitation>, List<FacebookUser>, FacebookAndInvitation>() {
            @Override
            public FacebookAndInvitation apply(List<UserInvitation> invited, List<FacebookUser> fbfriends) throws Exception {
                return new FacebookAndInvitation(fbfriends, invited);
            }
        });


    }
    public void randomtest(){
        Log.d("PARTYID", mParty.getId());
        String accessToken = "";
        String facebook_id = "";
        SharedPreferences mPrefs = getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }
        if (mPrefs.contains("facebook_id")){
            facebook_id = mPrefs.getString("facebook_id", "");
        }
        GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + facebook_id + "/friends");
        Gson gson = new Gson();
        Disposable friendsObserver = Single.defer(()->Single.just(request.executeAndWait()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(t -> {
                    Log.d("WTF", t.toString());
                    ArrayList<FacebookUser> friends = gson.fromJson(t.getJSONObject().getJSONArray("data").toString(), new TypeToken<ArrayList<FacebookUser>>(){}.getType());
                    return friends;
                })
                .subscribe(t-> {
                    Log.d("t works", "tworks");
                });

        Single<List<UserInvitation>> invitationsObserver = service.getUsersInvited("Bearer "+accessToken, mParty.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        compositeDisposable.add(friendsObserver);
    }


    public class FacebookAndInvitation {

        public FacebookAndInvitation(List<FacebookUser> fbUsers, List<UserInvitation> uInvitations) {
            this.fbUsers = fbUsers;
            this.uInvitations = uInvitations;
        }

        public List<FacebookUser> fbUsers;
        public List<UserInvitation> uInvitations;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    @Override
    protected void onPause(){
        super.onPause();
        compositeDisposable.clear();
    }

}
