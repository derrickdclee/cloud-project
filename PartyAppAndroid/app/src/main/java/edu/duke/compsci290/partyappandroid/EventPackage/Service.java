package edu.duke.compsci290.partyappandroid.EventPackage;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by kennethkoch on 4/15/18.
 */

public interface Service {
    @Multipart
    @POST("/parties/")
    Call<ResponseBody> postImage(@Header("Authorization") String token,
                                 @Part("name") RequestBody name,
                                 @Part("description") RequestBody description,
                                 @Part("lat") RequestBody lat,
                                 @Part("lng") RequestBody lng,
                                 @Part("start_time") RequestBody start_time,
                                 @Part("end_time") RequestBody end_time,
                                 @Part MultipartBody.Part image);
}
