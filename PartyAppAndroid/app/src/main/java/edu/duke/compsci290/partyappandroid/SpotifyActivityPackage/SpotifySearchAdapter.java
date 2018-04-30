package edu.duke.compsci290.partyappandroid.SpotifyActivityPackage;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.duke.compsci290.partyappandroid.R;
import edu.duke.compsci290.partyappandroid.SpotifyPackage.PlaySongListener;
import edu.duke.compsci290.partyappandroid.SpotifyPackage.SpotifyService;
import edu.duke.compsci290.partyappandroid.SpotifyPackage.SpotifySong;

/**
 * Created by kennethkoch on 4/26/18.
 */

public class SpotifySearchAdapter extends RecyclerView.Adapter<SpotifySearchAdapter.ViewHolder>{
    private ArrayList<SpotifySong> mSpotifySongs;
    private String partyId;
    private Context mContext;
    private PlaySongListener mListener;
    private FirebaseFunctions mFunctions;
    private boolean ishost;
    public SpotifySearchAdapter(Context context, ArrayList<SpotifySong> songs, String pid, PlaySongListener listener, boolean ishost){
        mContext = context;
        mSpotifySongs = songs;
        partyId = pid;
        mListener = listener;
        mFunctions = FirebaseFunctions.getInstance();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mAlbumImage;
        public TextView mSongTitle;
        public TextView mAlbumTitle;
        public TextView mArtistName;
        public Button mPlayNowButton;
        public Button mSuggestButton;
        public ViewHolder(View itemView) {
            super(itemView);
            mAlbumImage = itemView.findViewById(R.id.song_album_image);
            mSongTitle = itemView.findViewById(R.id.song_holder_title);
            mAlbumTitle = itemView.findViewById(R.id.song_holder_album);
            mArtistName = itemView.findViewById(R.id.song_holder_artist);
            mPlayNowButton = itemView.findViewById(R.id.song_holder_play_now_button);
            mSuggestButton = itemView.findViewById(R.id.song_holder_suggest_button);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = mInflater.inflate(R.layout.song_suggestion_holder, parent, false);
        final ViewHolder songHolder = new ViewHolder(row);
        songHolder.mPlayNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSong(mSpotifySongs.get(songHolder.getAdapterPosition()).getSpotifyUrl());
            }
        });
        if (!ishost){
            songHolder.mPlayNowButton.setVisibility(View.GONE);
        }
        songHolder.mSuggestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                suggestSong(mSpotifySongs.get(songHolder.getAdapterPosition())).addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        Log.d("TEST", task.getResult());
                    }
                });
            }
        });
        return songHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mSongTitle.setText(mSpotifySongs.get(position).getSongTitle());
        holder.mArtistName.setText(mSpotifySongs.get(position).getArtistName());
        holder.mAlbumTitle.setText(mSpotifySongs.get(position).getAlbumTitle());
        Picasso.get().load(mSpotifySongs.get(position).getAlbumImageUrl()).into(holder.mAlbumImage);
    }

    @Override
    public int getItemCount() {
        if (mSpotifySongs == null){
            return 0;
        }
        return mSpotifySongs.size();
    }

    private void playSong(String songurl){
        mListener.playSong(songurl);
    }

    private Task<String> suggestSong(SpotifySong song) {
        // Create the arguments to the callable function, which is just one string
        Map<String, Object> songRequest = new HashMap<>();
        songRequest.put("album_image", song.getAlbumImageUrl());
        songRequest.put("album_title", song.getAlbumTitle());
        songRequest.put("artist_name", song.getArtistName());
        songRequest.put("party_id", partyId);
        songRequest.put("song_title", song.getSongTitle());
        songRequest.put("spotify_url", song.getSpotifyUrl());

        return mFunctions
                .getHttpsCallable("suggestSong")
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
