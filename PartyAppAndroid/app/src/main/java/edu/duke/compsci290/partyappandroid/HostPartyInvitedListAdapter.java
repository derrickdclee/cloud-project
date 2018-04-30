package edu.duke.compsci290.partyappandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

import edu.duke.compsci290.partyappandroid.EventPackage.FacebookUser;
import edu.duke.compsci290.partyappandroid.EventPackage.InviteFilterStatus;
import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.PartyInvite;
import edu.duke.compsci290.partyappandroid.EventPackage.Service;
import edu.duke.compsci290.partyappandroid.EventPackage.User;
import edu.duke.compsci290.partyappandroid.EventPackage.UserInvitation;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by kennethkoch on 3/27/18.
 */

public class HostPartyInvitedListAdapter extends RecyclerView.Adapter<HostPartyInvitedListAdapter.ViewHolder> {
    private ArrayList<UserInvitation> mDisplayedInvitees;
    private Context mContext;
    private Service service;
    private InviteFilterStatus mFilterType;
    private PartyInvite mParty;

    public HostPartyInvitedListAdapter(Context context, ArrayList<UserInvitation> friends, PartyInvite party, InviteFilterStatus type){
        mDisplayedInvitees = friends;
        mContext = context;
        mFilterType = type;
        mParty = party;
        setupretrofit();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout mLinearLayout;
        public ImageView mFacebookThumbnail;
        public Button mAddButton;
        public Button mRemoveButton;
        public TextView mFriendName;
        public Button mPromoteToBouncer;
        public ViewHolder(View itemView) {
            super(itemView);
            mLinearLayout = itemView.findViewById(R.id.facebook_friend_linear_layout);
            mFacebookThumbnail = itemView.findViewById(R.id.facebook_thumbnail);
            mAddButton = itemView.findViewById(R.id.facebook_add);
            mRemoveButton = itemView.findViewById(R.id.facebook_remove);
            mFriendName = itemView.findViewById(R.id.facebook_name);
            mPromoteToBouncer = itemView.findViewById(R.id.promote_to_bouncer_button);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = mInflater.inflate(R.layout.host_party_fb_friends_holder, parent, false);
        final ViewHolder partyInviteeHolder = new ViewHolder(row);

        partyInviteeHolder.mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFriendFromParty(mDisplayedInvitees.get(partyInviteeHolder.getAdapterPosition()));
            }
        });
        partyInviteeHolder.mPromoteToBouncer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promoteToBouncer(mDisplayedInvitees.get(partyInviteeHolder.getAdapterPosition()));
            }
        });

        return partyInviteeHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //holder.mFriendName.setText(mDisplayedUsers.get(position).getUserName());
        holder.mFriendName.setText(mDisplayedInvitees.get(position).getInvitee().getFull_name());
        Picasso.get().load("http://graph.facebook.com/" + mDisplayedInvitees.get(position).getInvitee().getFacebook_id() + "/picture?type=square").into(holder.mFacebookThumbnail);
        holder.mLinearLayout.removeView(holder.mAddButton);
        switch (mFilterType) {
            case INVNITED:
                Log.d("WORKING", "WTF");
                holder.mPromoteToBouncer.setVisibility(View.GONE);
                break;
            case RSVP:
                holder.mRemoveButton.setVisibility(View.GONE);
                break;
            case CHECKEDIN:
                holder.mRemoveButton.setVisibility(View.GONE);
                holder.mPromoteToBouncer.setVisibility(View.GONE);
                break;
        }

    }

    @Override
    public int getItemCount() {
        if (mDisplayedInvitees == null){
            return 0;
        }
        return mDisplayedInvitees.size();
    }


    private void removeFriendFromParty(UserInvitation invitee){
        int indexToRemove = mDisplayedInvitees.indexOf(invitee);

        String accessToken = "";
        SharedPreferences mPrefs = mContext.getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }

        retrofit2.Call<Response<Void>> req = service.removeInvitee("Bearer "+accessToken, invitee.getId());
        req.enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, Response<Response<Void>> response) {
                mDisplayedInvitees.remove(indexToRemove);
                notifyItemRemoved(indexToRemove);
                notifyItemRangeChanged(indexToRemove, mDisplayedInvitees.size());
            }

            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {

            }
        });
    }

    private void promoteToBouncer(UserInvitation invitee){
        int indexToRemove = mDisplayedInvitees.indexOf(invitee);
        String accessToken = "";
        SharedPreferences mPrefs = mContext.getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }
        Log.d("bouncerid", invitee.getInvitee().getFacebook_id());
        service.addBouncerToParty("Bearer "+accessToken, mParty.getId(), invitee.getInvitee().getFacebook_id())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        mDisplayedInvitees.remove(indexToRemove);
                        notifyItemRemoved(indexToRemove);
                        notifyItemRangeChanged(indexToRemove, mDisplayedInvitees.size());
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });

    }

    private void setupretrofit(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        service = new Retrofit.Builder().baseUrl("http://party-app-dev.us-west-2.elasticbeanstalk.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(Service.class);
    }

    public void clear(){
        if (mDisplayedInvitees==null){
            return;
        }
        mDisplayedInvitees.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<UserInvitation> uinvs){
        if (mDisplayedInvitees==null){
            return;
        }
        mDisplayedInvitees.addAll(uinvs);
        notifyDataSetChanged();
    }

}
