package edu.duke.compsci290.partyappandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;

import edu.duke.compsci290.partyappandroid.EventPackage.MyDeletePartyListener;
import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.PartyInvite;
import edu.duke.compsci290.partyappandroid.EventPackage.User;

/**
 * Created by kennethkoch on 3/23/18.
 */

public class HostAdapter extends RecyclerView.Adapter<HostAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<PartyInvite> mParties;
    public HostAdapter(Context context, ArrayList<PartyInvite> parties){
        mContext = context;
        mParties = parties;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout mLinearLayout;
        public Button mEditButton;
        public Button mScanButton;
        public Button mDeleteButton;
        public TextView mPartyName;
        public TextView mPartyDescription;
        public ImageView mPartyImage;
        public ViewHolder(View itemView) {
            super(itemView);
            mLinearLayout = itemView.findViewById(R.id.party_holder_linear_layout);
            mEditButton = itemView.findViewById(R.id.party_edit_button);
            mScanButton = itemView.findViewById(R.id.party_scan_button);
            mDeleteButton = itemView.findViewById(R.id.party_delete_button);
            mPartyName = itemView.findViewById(R.id.party_name_text);
            mPartyDescription = itemView.findViewById(R.id.party_description_text);
            mPartyImage = itemView.findViewById(R.id.party_image);
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = mInflater.inflate(R.layout.host_party_holder, parent, false);
        final ViewHolder partyHolder = new ViewHolder(row);
        partyHolder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToHostParty(mParties.get(partyHolder.getAdapterPosition()));
            }
        });
        partyHolder.mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToScannerActivity(mParties.get(partyHolder.getAdapterPosition()));
            }
        });
        partyHolder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeParty(mParties.get(partyHolder.getAdapterPosition()));
            }
        });
        return partyHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mPartyName.setText(mParties.get(position).getName());
        holder.mPartyDescription.setText(mParties.get(position).getDescription());
        Picasso.get().load(mParties.get(position).getImage()).into(holder.mPartyImage);
    }

    @Override
    public int getItemCount() {
        return mParties.size();
    }

    private void goToHostParty(PartyInvite party){
        Intent intent = new Intent(mContext, HostPartyActivity.class);
        intent.putExtra("party_object", (Serializable) party);
        mContext.startActivity(intent);

    }
    private void goToScannerActivity(PartyInvite party){
        Intent intent = new Intent(mContext, PartyScanActivity.class);
        intent.putExtra("party_id", party.getId());
        mContext.startActivity(intent);
    }
    private void removeParty(PartyInvite party){
        ((MyDeletePartyListener)mContext).callback(party.getId());
    }

}

