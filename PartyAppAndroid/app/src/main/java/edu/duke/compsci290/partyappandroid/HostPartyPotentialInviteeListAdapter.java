package edu.duke.compsci290.partyappandroid;

import android.content.Context;
import android.content.SharedPreferences;
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

import edu.duke.compsci290.partyappandroid.EventPackage.FacebookUser;
import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.Service;
import edu.duke.compsci290.partyappandroid.EventPackage.UserInvitation;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by kennethkoch on 3/27/18.
 */

public class HostPartyPotentialInviteeListAdapter extends RecyclerView.Adapter<HostPartyPotentialInviteeListAdapter.ViewHolder> {
    private ArrayList<FacebookUser> mDisplayedUsers;
    private Context mContext;
    private Service service;
    private Party mParty;
    private ArrayList<UserInvitation> mUserInvitationList;
    public HostPartyPotentialInviteeListAdapter(Context context, ArrayList<FacebookUser> friends, Party party){
        mDisplayedUsers = friends;
        mContext = context;
        mParty = party;
        mUserInvitationList = new ArrayList<>();
        setupretrofit();
        //getInviteeInfo();
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
        fbFriendHolder.mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriendToParty(mDisplayedUsers.get(fbFriendHolder.getAdapterPosition()));
            }
        });
        return fbFriendHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //holder.mFriendName.setText(mDisplayedUsers.get(position).getUserName());
        holder.mFriendName.setText(mDisplayedUsers.get(position).getName());
        Picasso.get().load("http://graph.facebook.com/" + mDisplayedUsers.get(position).getId() + "/picture?type=square").into(holder.mFacebookThumbnail);
        holder.mLinearLayout.removeView(holder.mRemoveButton);
    }

    @Override
    public int getItemCount() {
        if (mDisplayedUsers == null){
            return 0;
        }
        return mDisplayedUsers.size();
    }

    private void addFriendToParty(FacebookUser friend){
        Log.d("DOES THIS HIT", "yes it does");
        int indexToRemove = mDisplayedUsers.indexOf(friend);

        String accessToken = "";
        SharedPreferences mPrefs = mContext.getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }

        /*
        service.inviteUser("Bearer "+accessToken, friend.getId(), mParty.getPartyId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t-> {
                    Log.d("RESPONSE", t.string());
                    mDisplayedUsers.remove(indexToRemove);
                    notifyItemRemoved(indexToRemove);
                    notifyItemRangeChanged(indexToRemove, mDisplayedUsers.size());
                }, e -> {
                    e.printStackTrace();
                });*/


        retrofit2.Call<okhttp3.ResponseBody> req = service.inviteUser("Bearer "+accessToken, friend.getId(), mParty.getPartyId());
        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.d("RESPONSE", response.message());
                mDisplayedUsers.remove(indexToRemove);
                notifyItemRemoved(indexToRemove);
                notifyItemRangeChanged(indexToRemove, mDisplayedUsers.size());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }


    private void setupretrofit(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        service = new Retrofit.Builder().baseUrl("http://party-app-dev.us-west-2.elasticbeanstalk.com").addConverterFactory(GsonConverterFactory.create(gson)).build().create(Service.class);
    }


}
