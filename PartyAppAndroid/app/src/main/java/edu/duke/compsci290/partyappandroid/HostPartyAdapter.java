package edu.duke.compsci290.partyappandroid;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import java.util.ArrayList;

import edu.duke.compsci290.partyappandroid.EventPackage.User;

/**
 * Created by kennethkoch on 3/27/18.
 */

public class HostPartyAdapter extends RecyclerView.Adapter<HostPartyAdapter.ViewHolder> {
    private ArrayList<User> mDisplayedUsers;
    private boolean mShowRemoveButton;
    private Context mContext;
    public HostPartyAdapter(Context context, ArrayList<User> friends, boolean showRemoveButton){
        mDisplayedUsers = friends;
        mShowRemoveButton = showRemoveButton;
        mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout mLinearLayout;
        public ImageView mFacebookThumbnail;
        public Button mAddButton;
        public Button mRemoveButton;
        public TextView mFriendName;

        public ViewHolder(View itemView) {
            super(itemView);
            mLinearLayout = itemView.findViewById(R.id.facebook_friend_linear_layout);
            mFacebookThumbnail = itemView.findViewById(R.id.facebook_thumbnail);
            mAddButton = itemView.findViewById(R.id.facebook_add);
            mRemoveButton = itemView.findViewById(R.id.facebook_remove);
            mFriendName = itemView.findViewById(R.id.facebook_name);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = mInflater.inflate(R.layout.host_party_fb_friends_holder, parent, false);
        final ViewHolder fbFriendHolder = new ViewHolder(row);
        if (!mShowRemoveButton){
            fbFriendHolder.mAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addFriendToParty(mDisplayedUsers.get(fbFriendHolder.getAdapterPosition()));
                }
            });
        }
        else{
            fbFriendHolder.mAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeFriendFromParty(mDisplayedUsers.get(fbFriendHolder.getAdapterPosition()));
                }
            });
        }
        return fbFriendHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mFriendName.setText(mDisplayedUsers.get(position).getUserName());
        Picasso.get().load("http://graph.facebook.com/" + mDisplayedUsers.get(position).getUserId() + "/picture?type=square").into(holder.mFacebookThumbnail);
        if (mShowRemoveButton){
            holder.mLinearLayout.removeView(holder.mAddButton);
        }
        else{
            holder.mLinearLayout.removeView(holder.mRemoveButton);
        }
    }

    @Override
    public int getItemCount() {
        return mDisplayedUsers.size();
    }

    private void addFriendToParty(User friend){
        int indexToRemove = mDisplayedUsers.indexOf(friend);
        mDisplayedUsers.remove(indexToRemove);
        notifyItemRemoved(indexToRemove);
        notifyItemRangeChanged(indexToRemove, mDisplayedUsers.size());
        /*
        HIT THAT DATABASE
         */
    }

    private void removeFriendFromParty(User friend){
        int indexToRemove = mDisplayedUsers.indexOf(friend);
        mDisplayedUsers.remove(indexToRemove);
        notifyItemRemoved(indexToRemove);
        notifyItemRangeChanged(indexToRemove, mDisplayedUsers.size());
        /*
        HIT THAT DATABASE
         */
    }

}
