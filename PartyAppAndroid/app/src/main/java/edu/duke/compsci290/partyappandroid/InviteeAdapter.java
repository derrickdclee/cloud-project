package edu.duke.compsci290.partyappandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;


import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.duke.compsci290.partyappandroid.EventPackage.PartyInvite;
import edu.duke.compsci290.partyappandroid.EventPackage.Service;
import edu.duke.compsci290.partyappandroid.EventPackage.UserInvitation;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

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
                goToInviteeActivity(mPartyInvites.get(partyHolder.getAdapterPosition()));

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

    private void goToInviteeActivity(PartyInvite partyInvite){
        Intent intent = new Intent(mContext, InviteePartyActivity.class);
        intent.putExtra("party_object", partyInvite);

        String accessToken = "";
        SharedPreferences mPrefs = mContext.getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Service service = new Retrofit.Builder().baseUrl("http://party-app-dev.us-west-2.elasticbeanstalk.com").addCallAdapterFactory(RxJava2CallAdapterFactory.create()).addConverterFactory(GsonConverterFactory.create(gson)).build().create(Service.class);
        service.getMyInvitation("Bearer "+accessToken, partyInvite.getId())
                .enqueue(new Callback<UserInvitation>() {
                    @Override
                    public void onResponse(Call<UserInvitation> call, Response<UserInvitation> response) {
                        intent.putExtra("user_invitation", response.body());
                        mContext.startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<UserInvitation> call, Throwable t) {

                    }
                });

    }

}
