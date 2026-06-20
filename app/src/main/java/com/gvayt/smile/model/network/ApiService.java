package com.gvayt.smile.model.network;

import com.gvayt.smile.model.network.dto.KidLoginResponse;
import com.gvayt.smile.model.network.dto.ParentLoginResponse;
import com.gvayt.smile.model.network.dto.ParentRegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api/auth/login/parent")
    Call<ParentLoginResponse> loginParent(@Header("Authorization") String credential);

    @GET("api/auth/login/kid")
    Call<KidLoginResponse> loginKid(@Header("Authorization") String credential);

    @POST("api/auth/register/parent")
    Call<ParentLoginResponse> registerParent(@Body ParentRegisterRequest request);
}
