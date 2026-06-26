package com.gvayt.smile.model.network;

import com.gvayt.smile.model.network.dto.kid.KidRegisterRequest;
import com.gvayt.smile.model.network.dto.kid.KidResponse;
import com.gvayt.smile.model.network.dto.parent.ParentResponse;
import com.gvayt.smile.model.network.dto.parent.ParentRegisterRequest;
import com.gvayt.smile.model.network.dto.task.TaskRequest;
import com.gvayt.smile.model.network.dto.task.TaskResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    // LOGIN

    @GET("api/auth/login/parent")
    Call<ParentResponse> loginParent(@Header("Authorization") String credential);

    @GET("api/auth/login/kid")
    Call<KidResponse> loginKid(@Header("Authorization") String credential);

    // REGISTER

    @POST("api/auth/register/parent")
    Call<ParentResponse> registerParent(@Body ParentRegisterRequest request);

    @POST("api/auth/register/kid")
    Call<KidResponse> registerKid(@Header("Authorization") String credential, @Body KidRegisterRequest request);

    // TASKS for PARENT

    @GET("api/tasks/kids/{login}")
    Call<List<TaskResponse>> getTaskMyKid(@Header("Authorization") String credential, @Path("login") String kidLogin);

    @POST("api/tasks/kids/{login}")
    Call<Void> addTaskMyKid(@Header("Authorization") String credential, @Path("login") String kidLogin, @Body TaskRequest taskRequest);

    @DELETE("api/tasks/kids/{login}/{id}")
    Call<Void> deleteTaskMyKid(@Header("Authorization") String credential, @Path("login") String kidLogin, @Path("id") long task_id);

    @GET("api/kids/{username}")
    Call<KidResponse> getKid(@Header("Authorization") String credential, @Path("username") String kidLogin);

    // TASKS for KID

    @GET("api/tasks")
    Call<List<TaskResponse>> getTask(@Header("Authorization") String credential);

    @POST("api/tasks")
    Call<TaskResponse> addTask(@Header("Authorization") String credential, @Body TaskRequest taskRequest);

    @DELETE("api/tasks/{id}")
    Call<Void> deleteTask(@Header("Authorization") String credential, @Path("id") long task_id);
}
