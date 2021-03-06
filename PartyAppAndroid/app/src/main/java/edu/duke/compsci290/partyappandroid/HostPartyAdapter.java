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
import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.Service;
import edu.duke.compsci290.partyappandroid.EventPackage.User;
import edu.duke.compsci290.partyappandroid.EventPackage.UserInvitation;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by kennethkoch on 3/27/18.
 */

public class HostPartyAdapter extends RecyclerView.Adapter<HostPartyAdapter.ViewHolder> {
    private ArrayList<FacebookUser> mDisplayedUsers;
    private boolean mShowRemoveButton;
    private Context mContext;
    private Service service;
    private Party mParty;
    private ArrayList<UserInvitation> mUserInvitationList;
    public HostPartyAdapter(Context context, ArrayList<FacebookUser> friends, Party party, boolean showRemoveButton){
        mDisplayedUsers = friends;
        mShowRemoveButton = showRemoveButton;
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
        if (!mShowRemoveButton){
            fbFriendHolder.mAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addFriendToParty(mDisplayedUsers.get(fbFriendHolder.getAdapterPosition()));
                }
            });
        }
        else{
            fbFriendHolder.mRemoveButton.setOnClickListener(new View.OnClickListener() {
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
        //holder.mFriendName.setText(mDisplayedUsers.get(position).getUserName());
        holder.mFriendName.setText(mDisplayedUsers.get(position).getName());
        Picasso.get().load("http://graph.facebook.com/" + mDisplayedUsers.get(position).getId() + "/picture?type=square").into(holder.mFacebookThumbnail);
        if (mShowRemoveButton){
            holder.mLinearLayout.removeView(holder.mAddButton);
        }
        else{
            holder.mLinearLayout.removeView(holder.mRemoveButton);
        }
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
        mDisplayedUsers.remove(indexToRemove);
        notifyItemRemoved(indexToRemove);
        notifyItemRangeChanged(indexToRemove, mDisplayedUsers.size());
        /*
        HIT THAT DATABASE
         */

        String accessToken = "";
        SharedPreferences mPrefs = mContext.getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }


        /*
        retrofit2.Call<okhttp3.ResponseBody> req = service.inviteUser("Bearer "+accessToken, friend.getId(), mParty.getPartyId());
        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.d("RESPONSE", response.message());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });*/

    }

    private void removeFriendFromParty(FacebookUser friend){
        int indexToRemove = mDisplayedUsers.indexOf(friend);
        mDisplayedUsers.remove(indexToRemove);
        notifyItemRemoved(indexToRemove);
        notifyItemRangeChanged(indexToRemove, mDisplayedUsers.size());

        String accessToken = "";
        SharedPreferences mPrefs = mContext.getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }
        /*
        HIT THAT DATABASE
         */
        //retrofit2.Call<okhttp3.ResponseBody> req = service.inviteUser("Bearer "+accessToken, friend.getId(), mParty.getPartyId());
        //service.removeInvitee("Bearer "+accessToken, friend.getId(), mParty.getPartyId());

    }

    private void setupretrofit(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        // Change base URL to your upload server URL.
        //service = new Retrofit.Builder().baseUrl("http://party-app-dev.us-west-2.elasticbeanstalk.com").client(client).build().create(Service.class);
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        service = new Retrofit.Builder().baseUrl("http://party-app-dev.us-west-2.elasticbeanstalk.com").addConverterFactory(GsonConverterFactory.create(gson)).build().create(Service.class);
    }

    private void getInviteeInfo(){
        /*
        String accessToken = "";
        SharedPreferences mPrefs = mContext.getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }

        service.getUsersInvited("Bearer "+accessToken, mParty.getPartyId()).enqueue(new Callback<List<UserInvitation>>() {
            @Override
            public void onResponse(Call<List<UserInvitation>> call, Response<List<UserInvitation>> response) {
                Log.d("RESPONSE", response.body().size()+"");
                mUserInvitationList = new ArrayList<>(response.body());
                for (int i=0;i<mUserInvitationList.size();i++){
                    Log.d("MORERESPONSE", mUserInvitationList.get(i).getInvitee());
                }
            }

            @Override
            public void onFailure(Call<List<UserInvitation>> call, Throwable t) {

            }
        });*/

    }

}
