package edu.duke.compsci290.partyappandroid;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.duke.compsci290.partyappandroid.EventPackage.PartyInvite;

/**
 * Created by kennethkoch on 4/20/18.
 */

public class InviteeAdapter extends RecyclerView.Adapter<InviteeAdapter.ViewHolder> {
    private ArrayList<PartyInvite> mPartyInvites;
    private Context mContext;
    public InviteeAdapter(Context context, ArrayList<PartyInvite> partyInvs){
        mContext = context;
        mPartyInvites = partyInvs;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImage;
        public TextView mNameText;
        public TextView mDescriptionText;
        public TextView mStartText;
        public TextView mEndText;
        public TextView mLocationText;
        public ViewHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.invitee_party_image);
            mNameText = itemView.findViewById(R.id.invitee_party_name_text);
            mDescriptionText = itemView.findViewById(R.id.invitee_party_description_text);
            mStartText = itemView.findViewById(R.id.invitee_party_start_time_text);
            mEndText = itemView.findViewById(R.id.invitee_party_end_time_text);
            mLocationText = itemView.findViewById(R.id.invitee_party_location_text);
        }
    }
    @Override
    public InviteeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = mInflater.inflate(R.layout.invitee_party_holder, parent, false);
        final ViewHolder partyHolder = new ViewHolder(row);
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, InviteePartyActivity.class);
                intent.putExtra("party_object", mPartyInvites.get(partyHolder.getAdapterPosition()));
                mContext.startActivity(intent);
            }
        });
        return partyHolder;
    }

    @Override
    public void onBindViewHolder(InviteeAdapter.ViewHolder holder, int position) {
        holder.mNameText.setText(mPartyInvites.get(position).getName());
        holder.mDescriptionText.setText(mPartyInvites.get(position).getDescription());
        holder.mStartText.setText(mPartyInvites.get(position).getStart_time());
        holder.mEndText.setText(mPartyInvites.get(position).getEnd_time());
        Geocoder myg = new Geocoder(mContext);
        String addr = "";
        try {
            List<Address> myaddr = myg.getFromLocation(Double.parseDouble(mPartyInvites.get(position).getLat()),
                    Double.parseDouble(mPartyInvites.get(position).getLng()), 1);
            for (int i=0;i<=myaddr.get(0).getMaxAddressLineIndex();i++){
                addr += myaddr.get(0).getAddressLine(i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.mLocationText.setText(addr);
        Picasso.get().load(mPartyInvites.get(position).getImage()).into(holder.mImage);
    }

    @Override
    public int getItemCount() {
        if (mPartyInvites==null){
            return 0;
        }
        return mPartyInvites.size();
    }

}
