package edu.duke.compsci290.partyappandroid.SpotifyActivityPackage;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import edu.duke.compsci290.partyappandroid.R;

/**
 * Created by kennethkoch on 4/26/18.
 */

public class SongVoteViewHolder extends RecyclerView.ViewHolder {
    public TextView mSongTitle;
    public ImageView mAlbumImage;
    public TextView mAlbumTitle;
    public TextView mArtistName;
    public Button mUpvote;
    public Button mDownvote;
    public TextView mScoreView;
    public SongVoteViewHolder(View itemView) {
        super(itemView);
        mSongTitle = itemView.findViewById(R.id.vote_song_holder_title);
        mAlbumImage = itemView.findViewById(R.id.vote_song_album_image);
        mAlbumTitle = itemView.findViewById(R.id.vote_song_holder_album);
        mArtistName = itemView.findViewById(R.id.vote_song_holder_artist);
        mUpvote = itemView.findViewById(R.id.vote_upvote_button);
        mDownvote = itemView.findViewById(R.id.vote_downvote_button);
        mScoreView = itemView.findViewById(R.id.vote_score);
    }
}
