package com.gvayt.smile.model;

import static com.gvayt.smile.Constant.KEY_TOKEN;

import com.gvayt.smile.contract.ParentContract;
import com.gvayt.smile.model.local.LocalStorage;
import com.gvayt.smile.model.network.ApiService;
import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.TypeApiError;
import com.gvayt.smile.model.network.dto.parent.ParentResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentModel implements ParentContract.Model {
    private final ApiService apiService;
    private final LocalStorage localStorage;

    public ParentModel(ApiService apiService, LocalStorage localStorage) {
        this.apiService = apiService;
        this.localStorage = localStorage;
    }

    @Override
    public void getParentInfo(ModelCallback<ParentResponse> callback) {
        apiService.loginParent(localStorage.getString(KEY_TOKEN, "")).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ParentResponse> call, Response<ParentResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                }
                else if (response.code() == 404 || response.code() == 401) {
                    callback.onError(TypeApiError.CLIENT);
                }
                else {
                    callback.onError(TypeApiError.SERVER);
                }
            }

            @Override
            public void onFailure(Call<ParentResponse> call, Throwable t) {
                callback.onError(TypeApiError.NETWORK);
            }
        });
    }

    @Override
    public void logout() {
        localStorage.clear();
    }
}
