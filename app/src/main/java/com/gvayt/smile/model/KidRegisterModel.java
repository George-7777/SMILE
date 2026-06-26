package com.gvayt.smile.model;

import static com.gvayt.smile.Constant.KEY_TOKEN;

import com.gvayt.smile.contract.KidRegisterContract;
import com.gvayt.smile.model.local.LocalStorage;
import com.gvayt.smile.model.network.ApiService;
import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.TypeApiError;
import com.gvayt.smile.model.network.dto.kid.KidRegisterRequest;
import com.gvayt.smile.model.network.dto.kid.KidResponse;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KidRegisterModel implements KidRegisterContract.Model {
    private final ApiService apiService;
    private final LocalStorage localStorage;

    public KidRegisterModel(ApiService apiService, LocalStorage localStorage) {
        this.apiService = apiService;
        this.localStorage = localStorage;
    }

    @Override
    public void registerKid(KidRegisterRequest kidRegisterRequest, ModelCallback<KidResponse> callback) {
        apiService.registerKid(localStorage.getString(KEY_TOKEN, ""), kidRegisterRequest).enqueue(new Callback<KidResponse>() {
            @Override
            public void onResponse(Call<KidResponse> call, Response<KidResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                }
                else if (response.code() == 400) {
                    callback.onError(TypeApiError.CLIENT);
                }
                else {
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
