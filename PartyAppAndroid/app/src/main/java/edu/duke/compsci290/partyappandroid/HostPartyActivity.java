package edu.duke.compsci290.partyappandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.widget.SwipeRefreshLayout;
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

import edu.duke.compsci290.partyappandroid.EventPackage.DjangoUser;
import edu.duke.compsci290.partyappandroid.EventPackage.FacebookUser;
import edu.duke.compsci290.partyappandroid.EventPackage.InviteFilterStatus;
import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.PartyInvite;
import edu.duke.compsci290.partyappandroid.EventPackage.PartyInviteSpecifics;
import edu.duke.compsci290.partyappandroid.EventPackage.Service;
import edu.duke.compsci290.partyappandroid.EventPackage.User;
import edu.duke.compsci290.partyappandroid.EventPackage.UserInvitation;
import edu.duke.compsci290.partyappandroid.SpotifyActivityPackage.PlaySpotifyActivity;
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


    private ArrayList<FacebookUser> mFriendsToInvite;
    private ArrayList<UserInvitation> mUserInvitations;


    private Button selectedButton;
    private RecyclerView rv;
    private Service service;
    private Button mGoToSpotifyButton;
    private String mAccessToken;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private HostPartyPotentialInviteeListAdapter mInviteeAdapter;
    private HostPartyInvitedListAdapter mInvitedAdapter;
    private HostPartyBouncerAdapter mBouncerAdapter;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_party);
        Intent intent = this.getIntent();
        mFriendsToInvite = new ArrayList<>();
        mUserInvitations = new ArrayList<>();

        setupretrofit();
        mParty = (PartyInvite) intent.getSerializableExtra("party_object");

        SharedPreferences mPrefs = getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            mAccessToken = mPrefs.getString("access_token", "");
        }


        mGoToSpotifyButton = findViewById(R.id.host_go_to_spotify_button);
        mGoToSpotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPartyPlaylist();
            }
        });

        final Button toInviteButton = findViewById(R.id.to_invite_button);
        final Button invitedButton = findViewById(R.id.invited_button);
        final Button rsvpedButton = findViewById(R.id.rsvped_button);
        final Button checkedInButton = findViewById(R.id.checked_in_button);
        final Button bouncerButton = findViewById(R.id.bouncers_button);
        selectedButton = toInviteButton;
        selectedButton.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
        selectedButton.invalidate();
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("this has refreshed", "success");
                refreshSwipeLayout();
            }
        });


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
        //rv.setAdapter(new HostPartyPotentialInviteeListAdapter(null, null, null));

        getPartySpecifics();
        Disposable serviceCall = executeRx2java().subscribe(t->{
            Log.d("IS THIS CALLED", "WELL IS IT");
            List<FacebookUser> friendsToInvite = t.fbUsers;
            Set<String> invitedIds = new HashSet<>();
            for (int i=0;i<t.uInvitations.size();i++){
                invitedIds.add(t.uInvitations.get(i).getInvitee().getFacebook_id());
            }
            for (int i=0;i<t.party.getBouncers().size();i++){
                invitedIds.add(t.party.getBouncers().get(i).getFacebook_id());
            }
            friendsToInvite.removeIf(user -> invitedIds.contains(user.getId()));
            mFriendsToInvite = (ArrayList<FacebookUser>) friendsToInvite;
            mUserInvitations = (ArrayList<UserInvitation>) t.uInvitations;
            mParty = t.party;
            mInviteeAdapter = new HostPartyPotentialInviteeListAdapter(this, (ArrayList<FacebookUser>) friendsToInvite, mParty);
            mInvitedAdapter = new HostPartyInvitedListAdapter(this, null, mParty, InviteFilterStatus.INVNITED);
            mBouncerAdapter = new HostPartyBouncerAdapter(this, null);
            rv.setAdapter(mInviteeAdapter);
        });
        compositeDisposable.add(serviceCall);
    }

    private void refreshSwipeLayout(){
        Disposable serviceCall = executeRx2java().subscribe(t->{
            Log.d("IS THIS CALLED", "WELL IS IT");
            List<FacebookUser> friendsToInvite = t.fbUsers;
            Set<String> invitedIds = new HashSet<>();
            for (int i=0;i<t.uInvitations.size();i++){
                invitedIds.add(t.uInvitations.get(i).getInvitee().getFacebook_id());
            }
            for (int i=0;i<t.party.getBouncers().size();i++){
                invitedIds.add(t.party.getBouncers().get(i).getFacebook_id());
            }
            friendsToInvite.removeIf(user -> invitedIds.contains(user.getId()));
            mFriendsToInvite = (ArrayList<FacebookUser>) friendsToInvite;
            mUserInvitations = (ArrayList<UserInvitation>) t.uInvitations;
            mInviteeAdapter.clear();
            mInviteeAdapter.addAll((ArrayList<FacebookUser>)friendsToInvite);
            mParty = t.party;
            mBouncerAdapter.clear();
            mBouncerAdapter.addAll((ArrayList<DjangoUser>) t.party.getBouncers());

            switch (selectedButton.getId()) {
                case R.id.invited_button:
                    mInvitedAdapter.clear();
                    mInvitedAdapter.addAll(mUserInvitations);
                    break;
                case R.id.rsvped_button:
                    List<UserInvitation> rsvped = new ArrayList<>(mUserInvitations);
                    rsvped.removeIf(user-> !user.getHas_rsvped());
                    mInvitedAdapter.clear();
                    mInvitedAdapter.addAll((ArrayList<UserInvitation>) rsvped);
                    break;
                case R.id.checked_in_button:
                    List<UserInvitation> checkedin = new ArrayList<>(t.uInvitations);
                    checkedin.removeIf(user-> !user.getHas_rsvped());
                    mInvitedAdapter.clear();
                    mInvitedAdapter.addAll((ArrayList<UserInvitation>) checkedin);
                    rv.setAdapter(new HostPartyInvitedListAdapter(this, (ArrayList<UserInvitation>)checkedin, mParty, InviteFilterStatus.CHECKEDIN));
                    break;
            }
            mSwipeRefreshLayout.setRefreshing(false);
        });
        compositeDisposable.add(serviceCall);
    }


    private void getPartySpecifics(){
        Disposable serviceCall = service.getPartySpecifics("Bearer "+ mAccessToken, mParty.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(party -> {
                    mParty = party;
                });
        compositeDisposable.add(serviceCall);
    }


    private void selectFriendsDisplay(Button pressedButton){
        if (!selectedButton.equals(pressedButton)){
            selectedButton.getBackground().clearColorFilter();
            selectedButton = pressedButton;
            selectedButton.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            selectedButton.invalidate();

            switch (selectedButton.getId()) {
                case R.id.to_invite_button:
                    mInviteeAdapter = new HostPartyPotentialInviteeListAdapter(this, (ArrayList<FacebookUser>) mFriendsToInvite, mParty);
                    rv.setAdapter(mInviteeAdapter);
                    break;
                case R.id.invited_button:
                    mInvitedAdapter = new HostPartyInvitedListAdapter(this, (ArrayList<UserInvitation>) mUserInvitations, mParty, InviteFilterStatus.INVNITED);
                    rv.setAdapter(mInvitedAdapter);
                    break;
                case R.id.rsvped_button:
                    List<UserInvitation> rsvped = new ArrayList<>(mUserInvitations);
                    rsvped.removeIf(user -> !user.getHas_rsvped());
                    mInvitedAdapter = new HostPartyInvitedListAdapter(this, (ArrayList<UserInvitation>) rsvped, mParty, InviteFilterStatus.RSVP);
                    rv.setAdapter(mInvitedAdapter);
                    break;
                case R.id.checked_in_button:
                    List<UserInvitation> checkedin = new ArrayList<>(mUserInvitations);
                    checkedin.removeIf(user -> !user.getHas_checkedin());
                    mInvitedAdapter = new HostPartyInvitedListAdapter(this, (ArrayList<UserInvitation>) checkedin, mParty, InviteFilterStatus.CHECKEDIN);
                    rv.setAdapter(mInvitedAdapter);
                    break;
                case R.id.bouncers_button:
                    Log.d("this is called", mParty.getBouncers().size()+"");
                    mBouncerAdapter = new HostPartyBouncerAdapter(this, mParty.getBouncers());
                    rv.setAdapter(mBouncerAdapter);
            }

            //refreshData();
        }
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
        Single<PartyInvite> bouncerObserver = service.getPartySpecifics("Bearer "+accessToken, mParty.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        return Single.zip(friendsObserver, invitationsObserver, bouncerObserver, (f, i, b) -> {
            return new FacebookAndInvitation(f, i, b);
        });

        /*
       return Single.zip(invitationsObserver, friendsObserver, new BiFunction<List<UserInvitation>, List<FacebookUser>, FacebookAndInvitation>() {
            @Override
            public FacebookAndInvitation apply(List<UserInvitation> invited, List<FacebookUser> fbfriends) throws Exception {
                return new FacebookAndInvitation(fbfriends, invited);
            }
        });*/


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

        public FacebookAndInvitation(List<FacebookUser> fbUsers, List<UserInvitation> uInvitations, PartyInvite party) {
            this.fbUsers = fbUsers;
            this.uInvitations = uInvitations;
            this.party = party;
        }

        public List<FacebookUser> fbUsers;
        public List<UserInvitation> uInvitations;
        public PartyInvite party;

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

    private void goToPartyPlaylist(){
        Intent intent = new Intent(this, PlaySpotifyActivity.class);
        intent.putExtra("party_id", mParty.getId());
        intent.putExtra("is_host", true);
        startActivity(intent);
    }

}
