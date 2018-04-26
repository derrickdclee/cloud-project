package edu.duke.compsci290.partyappandroid.SpotifyActivityPackage;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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
    public SpotifySearchAdapter(Context context, ArrayList<SpotifySong> songs, String pid, PlaySongListener listener){
        mContext = context;
        mSpotifySongs = songs;
        partyId = pid;
        mListener = listener;
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

}
