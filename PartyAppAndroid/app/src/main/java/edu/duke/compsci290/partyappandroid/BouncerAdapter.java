package edu.duke.compsci290.partyappandroid;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import edu.duke.compsci290.partyappandroid.EventPackage.PartyInvite;
import edu.duke.compsci290.partyappandroid.SpotifyActivityPackage.PlaySpotifyActivity;

/**
 * Created by kennethkoch on 4/20/18.
 */

public class BouncerAdapter extends RecyclerView.Adapter<BouncerAdapter.ViewHolder> {
    private ArrayList<PartyInvite> mPartyInvites;
    private Context mContext;
    public BouncerAdapter(Context context, ArrayList<PartyInvite> partyInvs){
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
        public Button mScanButton;
        public Button mDirectionsButton;
        private Button mSpotifyButton;
        public ViewHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.bouncer_party_image);
            mNameText = itemView.findViewById(R.id.bouncer_party_name_text);
            mDescriptionText = itemView.findViewById(R.id.bouncer_party_description_text);
            mStartText = itemView.findViewById(R.id.bouncer_party_start_time_text);
            mEndText = itemView.findViewById(R.id.bouncer_party_end_time_text);
            mLocationText = itemView.findViewById(R.id.bouncer_party_location_text);
            mScanButton = itemView.findViewById(R.id.bouncer_scan_button);
            mDirectionsButton = itemView.findViewById(R.id.bouncer_directions_button);
            mSpotifyButton = itemView.findViewById(R.id.bouncer_music_button);
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = mInflater.inflate(R.layout.bouncer_party_holder, parent, false);
        final ViewHolder partyHolder = new ViewHolder(row);
        return partyHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mNameText.setText(mPartyInvites.get(position).getName());
        holder.mDescriptionText.setText(mPartyInvites.get(position).getDescription());
        SimpleDateFormat sdfForDjango = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssXXX", Locale.getDefault());
        SimpleDateFormat betterDateFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        String startTime = "Start Time: ";
        String endTime = "End Time: ";
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdfForDjango.parse(mPartyInvites.get(position).getStart_time()));
            startTime += betterDateFormat.format(c.getTime());
            c.setTime(sdfForDjango.parse(mPartyInvites.get(position).getEnd_time()));
            endTime += betterDateFormat.format(c.getTime());
        } catch (ParseException e){
            Log.d("PARSEECXEPTION", e.toString());
        }
        holder.mStartText.setText(startTime);
        holder.mEndText.setText(endTime);
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
        String finalAddr = addr;
        holder.mDirectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMaps(finalAddr, mPartyInvites.get(position).getLat(), mPartyInvites.get(position).getLng());
            }
        });
        holder.mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToScan(mPartyInvites.get(position));
            }
        });
        holder.mSpotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSpotify(mPartyInvites.get(position));
            }
        });
    }

    private void goToMaps(String address, String lat, String lng){
        if (address.equals("")){
            String newaddress = address.replace(" ", "+");
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+newaddress);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            mContext.startActivity(mapIntent);
        }
        else {
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+ lat +","+ lng);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            mContext.startActivity(mapIntent);
        }
    }

    private void goToScan(PartyInvite party){
        Intent intent = new Intent(mContext, PartyScanActivity.class);
        intent.putExtra("party_id", party.getId());
        mContext.startActivity(intent);
    }

    private void goToSpotify(PartyInvite party){
        Intent intent = new Intent(mContext, PlaySpotifyActivity.class);
        intent.putExtra("party_id", party.getId());
        intent.putExtra("is_host", false);
        mContext.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        if (mPartyInvites==null){
            return 0;
        }
        return mPartyInvites.size();
    }

}
