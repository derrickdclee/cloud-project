package edu.duke.compsci290.partyappandroid;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;

import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.User;

/**
 * Created by kennethkoch on 3/23/18.
 */

public class HostAdapter extends RecyclerView.Adapter<HostAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Party> mParties;
    private ArrayList<User> mUserFriends;
    public HostAdapter(Context context, ArrayList<Party> parties){
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
        public ViewHolder(View itemView) {
            super(itemView);
            mLinearLayout = itemView.findViewById(R.id.party_holder_linear_layout);
            mEditButton = itemView.findViewById(R.id.party_edit_button);
            mScanButton = itemView.findViewById(R.id.party_scan_button);
            mDeleteButton = itemView.findViewById(R.id.party_delete_button);
            mPartyName = itemView.findViewById(R.id.party_name_text);
            mPartyDescription = itemView.findViewById(R.id.party_description_text);
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
        return partyHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mPartyName.setText(mParties.get(position).getPartyName());
        holder.mPartyDescription.setText(mParties.get(position).getPartyDescription());
    }

    @Override
    public int getItemCount() {
        return mParties.size();
    }

    private void goToHostParty(Party party){
        Intent intent = new Intent(mContext, HostPartyActivity.class);
        intent.putExtra("party_object", (Serializable) party);
        intent.putExtra("user_friends", mUserFriends);
        mContext.startActivity(intent);

    }
    private void goToScannerActivity(Party party){
        Intent intent = new Intent(mContext, PartyScanActivity.class);
        intent.putExtra("party_object", (Serializable) party);
        mContext.startActivity(intent);
    }

    public void setUserFriends(ArrayList<User> friends){
        mUserFriends = friends;
    }


}
