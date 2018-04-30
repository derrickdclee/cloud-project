package edu.duke.compsci290.partyappandroid.SpotifyPackage;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Created by kennethkoch on 4/26/18.
 */

public interface SpotifyService {
    @GET("/v1/search/")
    Call<JsonObject> getSpotifySongs(@Header("Authorization") String token,
                                        @Query("q") String q,
                                        @Query("type") String type,
                                        @Query("limit") int limit);
    @GET("/v1/search/")
    Call<JsonObject> test(@Header("Authorization") String token);
}
