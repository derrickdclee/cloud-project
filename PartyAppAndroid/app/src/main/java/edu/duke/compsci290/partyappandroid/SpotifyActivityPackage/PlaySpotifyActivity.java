package edu.duke.compsci290.partyappandroid.SpotifyActivityPackage;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.duke.compsci290.partyappandroid.R;
import edu.duke.compsci290.partyappandroid.SpotifyPackage.PlaySongListener;
import edu.duke.compsci290.partyappandroid.SpotifyPackage.SongVote;
import edu.duke.compsci290.partyappandroid.SpotifyPackage.SpotifyService;
import edu.duke.compsci290.partyappandroid.SpotifyPackage.SpotifySong;
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

public class PlaySpotifyActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback, PlaySongListener {

    private static final String CLIENT_ID = "5c550f07c23a4f29b047759c98fdaf29";
    private static final String REDIRECT_URI = "edu.duke.compsci290.partyappandroid://callback";
    private Player mPlayer;
    private static final int REQUEST_CODE = 1337;
    private FirebaseFirestore mDb;
    private Button mSearchButton;
    private EditText mSongTitleEdit;
    private EditText mArtistEdit;
    private EditText mAlbumEdit;
    private SpotifyService mSpotifyService;
    private String mAccessToken;
    private RecyclerView rv;
    private String partyId;
    private FirestoreRecyclerAdapter mFirestoreAdapter;
    private Button mControlSongsButton;
    private Button mSuggestSongsButton;
    private Button mSelectedButton;
    private FirebaseFunctions mFunctions;
    private Button mPlayButton;
    private Button mRewindButton;
    private Button mFastForwardButton;
    private LinearLayout mSongLayout;
    private LinearLayout mArtistLayout;
    private LinearLayout mAlbumLayout;
    private RecyclerView mSongQueueRecyclerView;
    private boolean isHost;
    private FirebaseAuth mAuth;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_play_spotify);
        Intent intent = getIntent();
        partyId = intent.getStringExtra("party_id");
        isHost = intent.getBooleanExtra("is_host", false);
        mAuth = FirebaseAuth.getInstance();
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        mSongTitleEdit = findViewById(R.id.host_song_edit_text);
        mArtistEdit = findViewById(R.id.host_artist_edit_text);
        mAlbumEdit = findViewById(R.id.host_album_edit_text);
        mControlSongsButton = findViewById(R.id.host_manage_songs_button);
        mSuggestSongsButton = findViewById(R.id.host_suggest_songs_button);
        mControlSongsButton.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
        mControlSongsButton.invalidate();
        mSelectedButton = mControlSongsButton;
        mFunctions = FirebaseFunctions.getInstance();
        mPlayButton = findViewById(R.id.host_play_button);
        mRewindButton = findViewById(R.id.host_rewind_button);
        mFastForwardButton = findViewById(R.id.host_fast_forward_button);
        mSongLayout = findViewById(R.id.host_suggest_title_linear_layout);
        mAlbumLayout = findViewById(R.id.host_suggest_album_linear_layout);
        mArtistLayout = findViewById(R.id.host_suggest_artist_linear_layout);

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseAndStart();
            }
        });
        mRewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rewindSong();
            }
        });
        mFastForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fastForwardSong();
            }
        });

        mControlSongsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMode(mControlSongsButton);
            }
        });
        mSuggestSongsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMode(mSuggestSongsButton);
            }
        });
        mDb = FirebaseFirestore.getInstance();
        mSearchButton = findViewById(R.id.host_search_for_song_button);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterSearchSongs();
            }
        });

        if (!isHost){
            mPlayButton.setVisibility(View.GONE);
            mRewindButton.setVisibility(View.GONE);
            mFastForwardButton.setVisibility(View.GONE);
        }

        setupRetrofit();
        rv = findViewById(R.id.host_search_song_recyclerview);
        mSongQueueRecyclerView = findViewById(R.id.host_queued_songs_recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(this));
        mSongQueueRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new SpotifySearchAdapter(this, null, partyId, this, isHost));
        mSongQueueRecyclerView.setAdapter(null);

    }


    private void setupRetrofit(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        mSpotifyService = new Retrofit.Builder().baseUrl("https://api.spotify.com")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(SpotifyService.class);
    }

    private void toggleMode(Button button){
        if (!button.equals(mSelectedButton)){
            button.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            button.invalidate();
            mSelectedButton.getBackground().clearColorFilter();
            mSelectedButton = button;

            switch (button.getId()){
                case R.id.host_manage_songs_button:
                    mSongLayout.setVisibility(View.VISIBLE);
                    mArtistLayout.setVisibility(View.VISIBLE);
                    mAlbumLayout.setVisibility(View.VISIBLE);
                    mSearchButton.setVisibility(View.VISIBLE);
                    mFirestoreAdapter.stopListening();
                    rv.setVisibility(View.VISIBLE);
                    mSongQueueRecyclerView.setVisibility(View.INVISIBLE);
                    break;
                case R.id.host_suggest_songs_button:
                    mSongLayout.setVisibility(View.INVISIBLE);
                    mArtistLayout.setVisibility(View.INVISIBLE);
                    mAlbumLayout.setVisibility(View.INVISIBLE);
                    mSearchButton.setVisibility(View.INVISIBLE);
                    rv.setVisibility(View.INVISIBLE);
                    mSongQueueRecyclerView.setVisibility(View.VISIBLE);
                    setUpFirebaseAdapter();
                    break;
            }
        }
    }

    private void filterSearchSongs(){
        Log.d("TRACK", mSongTitleEdit.getText().toString());
        Log.d("ARTIST", mArtistEdit.getText().toString());
        Log.d("ALBUM", mAlbumEdit.getText().toString());
        if (mSongTitleEdit.getText().equals("")){
            Toast.makeText(this, "Must fill out title", Toast.LENGTH_SHORT);
            return;
        }
        String queryString = "";
        String title = mSongTitleEdit.getText().toString();
        queryString += "track:"+title;
        String artist = mArtistEdit.getText().toString();
        String album = mAlbumEdit.getText().toString();
        if (!artist.equals("")){
            queryString+=" artist:"+artist;
        }
        if (!album.equals("")){
            queryString+=" album:"+album;
        }
        Log.d("QUERYSTRING", queryString);

        mSpotifyService.getSpotifySongs("Bearer " + mAccessToken,
                queryString, "track", 5).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d("CALL", call.toString());
                if (response.body() == null){
                    return;
                }
                Log.d("RESPONSEBODY", response.body().toString());
                JsonObject track = response.body().getAsJsonObject("tracks");
                JsonArray items = track.getAsJsonArray("items");
                ArrayList<SpotifySong> returnedSongs = new ArrayList<>();
                if (items.size()>0){
                    mSongTitleEdit.setText("");
                    mArtistEdit.setText("");
                    mAlbumEdit.setText("");
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                for (int i=0; i<items.size();i++){
                    JsonObject item = items.get(i).getAsJsonObject();
                    String songname = item.get("name").getAsString();
                    String songuri = item.get("uri").getAsString();
                    JsonObject album = item.getAsJsonObject("album");
                    String albumname = album.get("name").getAsString();
                    JsonArray artists = item.getAsJsonArray("artists");
                    String artistname = "";
                    if (artists.size()>0){
                        artistname = artists.get(0).getAsJsonObject().get("name").getAsString();
                    }
                    JsonArray images = album.getAsJsonArray("images");
                    String imageurl = "";
                    if (images!=null && images.size()>0){
                        imageurl = images.get(images.size()-1).getAsJsonObject().get("url").getAsString();
                    }
                    SpotifySong potentialSong = new SpotifySong();
                    potentialSong.setSongTitle(songname);
                    potentialSong.setSpotifyUrl(songuri);
                    potentialSong.setAlbumTitle(albumname);
                    potentialSong.setArtistName(artistname);
                    potentialSong.setAlbumImageUrl(imageurl);
                    returnedSongs.add(potentialSong);
                }

                setNewSongAdapter(returnedSongs);

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Log.d("ACCESSTOKEN", response.getAccessToken());
                mAccessToken = response.getAccessToken();
                if (isHost){
                    Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                        @Override
                        public void onInitialized(SpotifyPlayer spotifyPlayer) {
                            mPlayer = spotifyPlayer;
                            mPlayer.addConnectionStateCallback(PlaySpotifyActivity.this);
                            mPlayer.addNotificationCallback(PlaySpotifyActivity.this);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mFirestoreAdapter!=null){
            mFirestoreAdapter.stopListening();
        }
        if (isHost){
            Spotify.destroyPlayer(this);
        }
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
        //mPlayer.playUri(null, "spotify:track:5ghIJDpPoe3CfHMGu71E6T", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d("MainActivity", "Received connection message: " + s);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            case kSpPlaybackNotifyTrackChanged:
                if (!mPlayer.getPlaybackState().isPlaying){
                    getTrackFromFirebase();
                }
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }


    private void getTrackFromFirebase(){
        mDb.collection("queued_songs").whereEqualTo("party_id", partyId)
                .orderBy("num_votes", Query.Direction.DESCENDING).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot.isEmpty()){
                        return;
                    }
                    else {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        mPlayer.playUri(null, doc.get("spotify_url").toString(), 0, 0);
                        removeSongFromTopOfQueue(doc.get("spotify_url").toString());
                    }
                }
                else {
                    Log.d("database issue", "issue");
                }
            }
        });

    }

    private void removeSongFromTopOfQueue(String songurl){
        mDb.collection("queued_songs")
                .whereEqualTo("party_id", partyId)
                .whereEqualTo("spotify_url", songurl)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots){
                            mDb.collection("queued_songs").document(doc.getId())
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("Deleted", "successfully deleted");
                                        }
                                    });
                        }
                    }
                });
        /*
        mDb.collection("queued_songs").document(songurl)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DELETED", "successfully deleted");
                    }
                });*/
    }

    private void pauseAndStart(){
        if (mPlayer.getPlaybackState().isPlaying){
            mPlayer.pause(null);
            //mPlayButton.setBackground(ContextCompat.getDrawable(this, R.drawable.));
        }
        else{
            if (mPlayer.getMetadata().currentTrack == null){
                getTrackFromFirebase();
            }
            else {
                Log.d("TRACK", mPlayer.getMetadata().currentTrack.toString());
                mPlayer.resume(null);
            }
        }
    }
    private void rewindSong(){
        mPlayer.skipToPrevious(null);
    }
    private void fastForwardSong(){
        mPlayer.skipToNext(null);
    }

    @Override
    public void playSong(String songId) {
        mPlayer.playUri(null, songId, 0, 0);
    }

    private void setNewSongAdapter(ArrayList<SpotifySong> songs){
        rv.setAdapter(new SpotifySearchAdapter(this, songs, partyId, this, isHost));
    }





    private void setUpFirebaseAdapter(){
        Query mQuery = FirebaseFirestore.getInstance().collection("queued_songs").whereEqualTo("party_id", partyId)
                .orderBy("num_votes", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<SongVote> options =
                new FirestoreRecyclerOptions.Builder<SongVote>()
                        .setQuery(mQuery, SongVote.class)
                        .build();
        mFirestoreAdapter = new FirestoreRecyclerAdapter<SongVote, SongVoteViewHolder>(options) {
            @Override
            protected void onBindViewHolder(SongVoteViewHolder holder, int position, final SongVote model) {
                holder.mScoreView.setText(model.getNum_votes()+"");
                holder.mSongTitle.setText(model.getSong_title());
                holder.mArtistName.setText(model.getArtist_name());
                holder.mAlbumTitle.setText(model.getAlbum_title());
                Picasso.get().load(model.getAlbum_image()).into(holder.mAlbumImage);
                if (model.getUpvoter_ids().contains(mAuth.getCurrentUser().getUid())){
                    holder.mUpvote.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
                    holder.mUpvote.invalidate();
                    holder.mDownvote.getBackground().clearColorFilter();
                }
                Log.d("Downvoters", model.getDownvoter_ids().toString());
                if (model.getDownvoter_ids().contains(mAuth.getCurrentUser().getUid())){
                    holder.mDownvote.getBackground().setColorFilter(Color.MAGENTA, PorterDuff.Mode.MULTIPLY);
                    holder.mDownvote.invalidate();
                    holder.mUpvote.getBackground().clearColorFilter();
                }
                holder.mUpvote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        voteOnSong(model.getSpotify_url(), true);
                    }
                });
                holder.mDownvote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        voteOnSong(model.getSpotify_url(), false);
                    }
                });
            }

            @Override
            public SongVoteViewHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.song_voting_holder, group, false);
                return new SongVoteViewHolder(view);
            }
        };

        mSongQueueRecyclerView.setAdapter(mFirestoreAdapter);
        mFirestoreAdapter.startListening();
    }


    private Task<String> voteOnSong(String spotifyurl, boolean is_upvote) {
        // Create the arguments to the callable function, which is just one string
        Map<String, Object> songRequest = new HashMap<>();
        songRequest.put("party_id", partyId);
        songRequest.put("is_upvote", is_upvote);
        songRequest.put("spotify_url", spotifyurl);

        return mFunctions
                .getHttpsCallable("voteOnSong")
                .call(songRequest)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.

                        String result = (String) task.getResult().getData();
                        Log.d("RESULT", result+ " ");
                        return result;
                    }
                });
    }


}
