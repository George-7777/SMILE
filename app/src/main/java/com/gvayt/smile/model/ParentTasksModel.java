package com.gvayt.smile.model;

import com.gvayt.smile.Constant;
import com.gvayt.smile.contract.ParentTasksContract;
import com.gvayt.smile.model.local.LocalStorage;
import com.gvayt.smile.model.network.ApiService;
import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.TypeApiError;
import com.gvayt.smile.model.network.dto.kid.KidResponse;
import com.gvayt.smile.model.network.dto.task.TaskRequest;
import com.gvayt.smile.model.network.dto.task.TaskResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentTasksModel implements ParentTasksContract.Model {
    private LocalStorage localStorage;
    private ApiService apiService;

    public ParentTasksModel(LocalStorage localStorage, ApiService apiService) {
        this.apiService = apiService;
        this.localStorage = localStorage;
    }

    @Override
    public void getTasksForKid(String kidLogin, ModelCallback<List<TaskResponse>> callback) {
        apiService.getTaskMyKid(localStorage.getString(Constant.KEY_TOKEN, ""), kidLogin).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<TaskResponse>> call, Response<List<TaskResponse>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(TypeApiError.SERVER);
                }
            }

            @Override
            public void onFailure(Call<List<TaskResponse>> call, Throwable t) {
                callback.onError(TypeApiError.NETWORK);
            }
        });
    }

    @Override
    public void addTaskForKid(String kidLogin, TaskRequest taskRequest, ModelCallback<Void> callback) {
        apiService.addTaskMyKid(localStorage.getString(Constant.KEY_TOKEN, ""), kidLogin, taskRequest).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(TypeApiError.SERVER);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(TypeApiError.NETWORK);
            }
        });
    }

    @Override
    public void deleteTaskForKid(String kidLogin, long task_id, ModelCallback<Void> callback) {
        apiService.deleteTaskMyKid(localStorage.getString(Constant.KEY_TOKEN, ""), kidLogin, task_id).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(TypeApiError.SERVER);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(TypeApiError.NETWORK);
            }
        });
    }

    @Override
    public void getKidInfo(String kidLogin, ModelCallback<KidResponse> callback) {
        apiService.getKid(localStorage.getString(Constant.KEY_TOKEN, ""), kidLogin).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<KidResponse> call, Response<KidResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(TypeApiError.SERVER);
                }
            }

            @Override
            public void onFailure(Call<KidResponse> call, Throwable t) {
                callback.onError(TypeApiError.NETWORK);
            }
        });
    }
}
