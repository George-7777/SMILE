package com.gvayt.smile.model.network;

import com.gvayt.smile.model.network.dto.KidResponse;
import com.gvayt.smile.model.network.dto.ParentResponse;
import com.gvayt.smile.model.network.dto.ParentRegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api/auth/login/parent")
    Call<ParentResponse> loginParent(@Header("Authorization") String credential);

    @GET("api/auth/login/kid")
    Call<KidResponse> loginKid(@Header("Authorization") String credential);

    @POST("api/auth/register/parent")
    Call<ParentResponse> registerParent(@Body ParentRegisterRequest request);
}
