package com.gvayt.smile.model;

import com.gvayt.smile.Constant;
import com.gvayt.smile.contract.TaskContract;
import com.gvayt.smile.model.local.LocalStorage;
import com.gvayt.smile.model.network.ApiService;
import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.TypeApiError;
import com.gvayt.smile.model.network.dto.task.TaskRequest;
import com.gvayt.smile.model.network.dto.task.TaskResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskModel implements TaskContract.Model {
    private final LocalStorage localStorage;
    private final ApiService apiService;

    public TaskModel(LocalStorage localStorage, ApiService apiService) {
        this.localStorage = localStorage;
        this.apiService = apiService;
    }

    @Override
    public void getTasks(ModelCallback<List<TaskResponse>> callback) {
        apiService.getTask(localStorage.getString(Constant.KEY_TOKEN, "")).enqueue(new Callback<>() {
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
    public void addTask(TaskRequest request, ModelCallback<TaskResponse> callback) {
        apiService.addTask(localStorage.getString(Constant.KEY_TOKEN, ""), request).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                if (response.isSuccessful())
                    callback.onSuccess(response.body());
                else
                    callback.onError(TypeApiError.SERVER);
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                callback.onError(TypeApiError.NETWORK);
            }
        });
    }

    @Override
    public void deleteTask(long taskId, ModelCallback<Void> callback) {
        apiService.deleteTask(localStorage.getString(Constant.KEY_TOKEN, ""), taskId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful())
                    callback.onSuccess(response.body());
                else
                    callback.onError(TypeApiError.SERVER);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(TypeApiError.NETWORK);
            }
        });
    }

    @Override
    public void saveTasksLocally(List<TaskResponse> tasks) throws IOException {
        localStorage.saveList(Constant.KEY_TASKS_KID, tasks, TaskResponse.class);
    }

    @Override
    public List<TaskResponse> getTasksLocally() throws IOException {
        System.out.println("ищем в localStorage");
        return localStorage.getList(Constant.KEY_TASKS_KID, TaskResponse.class, new ArrayList<>());
    }

    @Override
    public boolean isLogged() {
        return localStorage.getBoolean(Constant.KEY_LOGGED_IN, false);
    }
}
