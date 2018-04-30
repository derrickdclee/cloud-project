package edu.duke.compsci290.partyappandroid;

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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import edu.duke.compsci290.partyappandroid.EventPackage.DjangoUser;
import edu.duke.compsci290.partyappandroid.EventPackage.UserInvitation;

/**
 * Created by kennethkoch on 4/30/18.
 */

public class HostPartyBouncerAdapter extends RecyclerView.Adapter<HostPartyBouncerAdapter.ViewHolder> {
    private Context mContext;
    private List<DjangoUser> mBouncers;
    public HostPartyBouncerAdapter(Context context, List<DjangoUser> bouncers){
        mContext = context;
        mBouncers = bouncers;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public Button facebookAdd;
        public Button facebookRemove;
        public Button makeBouncer;
        public TextView mName;
        public ImageView mFacebookImage;
        public ViewHolder(View itemView) {
            super(itemView);
            facebookAdd = itemView.findViewById(R.id.facebook_add);
            facebookRemove = itemView.findViewById(R.id.facebook_remove);
            makeBouncer = itemView.findViewById(R.id.promote_to_bouncer_button);
            mName = itemView.findViewById(R.id.facebook_name);
            mFacebookImage = itemView.findViewById(R.id.facebook_thumbnail);
        }
    }
    @NonNull
    @Override
    public HostPartyBouncerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = mInflater.inflate(R.layout.host_party_fb_friends_holder, parent, false);
        final ViewHolder bouncerHolder = new ViewHolder(row);
        return bouncerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HostPartyBouncerAdapter.ViewHolder holder, int position) {
        holder.mName.setText(mBouncers.get(position).getFull_name());
        Picasso.get().load("http://graph.facebook.com/" + mBouncers.get(position).getFacebook_id() + "/picture?type=square").into(holder.mFacebookImage);
        holder.facebookRemove.setVisibility(View.GONE);
        holder.facebookAdd.setVisibility(View.GONE);
        holder.makeBouncer.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mBouncers.size();
    }

    public void clear(){
        if (mBouncers==null){
            return;
        }
        mBouncers.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<DjangoUser> users){
        if (mBouncers==null){
            return;
        }
        mBouncers.addAll(users);
        notifyDataSetChanged();
    }


}
