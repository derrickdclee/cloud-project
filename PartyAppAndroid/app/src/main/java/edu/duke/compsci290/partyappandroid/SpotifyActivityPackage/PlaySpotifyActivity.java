package edu.duke.compsci290.partyappandroid.SpotifyActivityPackage;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.firebase.firestore.FirebaseFirestore;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.duke.compsci290.partyappandroid.R;
import edu.duke.compsci290.partyappandroid.SpotifyPackage.PlaySongListener;
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
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_spotify);
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        mSongTitleEdit = findViewById(R.id.host_song_edit_text);
        mArtistEdit = findViewById(R.id.host_artist_edit_text);
        mAlbumEdit = findViewById(R.id.host_album_edit_text);
        mDb = FirebaseFirestore.getInstance();
        mSearchButton = findViewById(R.id.host_search_for_song_button);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterSearchSongs();
            }
        });
        Intent intent = getIntent();
        partyId = intent.getStringExtra("party_id");
        setupRetrofit();
        rv = findViewById(R.id.host_search_song_recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new SpotifySearchAdapter(this, null, partyId, this));

    }
    private void setupRetrofit(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        mSpotifyService = new Retrofit.Builder().baseUrl("https://api.spotify.com")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(SpotifyService.class);
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

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
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
            default:
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

    @Override
    public void playSong(String songId) {
        mPlayer.playUri(null, songId, 0, 0);
    }

    private void setNewSongAdapter(ArrayList<SpotifySong> songs){
        rv.setAdapter(new SpotifySearchAdapter(this, songs, partyId, this));
    }
}
