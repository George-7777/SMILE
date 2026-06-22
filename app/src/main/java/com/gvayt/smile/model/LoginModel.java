package com.gvayt.smile.model;

import static com.gvayt.smile.Constant.KEY_FIO;
import static com.gvayt.smile.Constant.KEY_LOGGED_IN;
import static com.gvayt.smile.Constant.KEY_PASSWORD;
import static com.gvayt.smile.Constant.KEY_ROLE;
import static com.gvayt.smile.Constant.KEY_TOKEN;
import static com.gvayt.smile.Constant.KEY_USERNAME;
import static com.gvayt.smile.Constant.KEY_USER_ID;

import com.gvayt.smile.contract.LoginContract;
import com.gvayt.smile.model.local.LocalStorage;
import com.gvayt.smile.model.network.ApiService;
import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.TypeApiError;
import com.gvayt.smile.model.network.dto.kid.KidResponse;
import com.gvayt.smile.model.network.dto.parent.ParentResponse;
import com.gvayt.smile.model.network.dto.parent.ParentRegisterRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Регистрация/вход через Basic auth и хранение в SharedPreferences
public class LoginModel implements LoginContract.Model {
    private static final Logger log = LoggerFactory.getLogger(LoginModel.class);
    private final ApiService apiService;
    private final LocalStorage localStorage;

    public LoginModel(ApiService apiService, LocalStorage localStorage) {
        this.apiService = apiService;
        this.localStorage = localStorage;
    }
    private String buildToken(String username, String password) {
        return Credentials.basic(username, password);
    }
    @Override
    public void loginParent(String username, String password, ModelCallback<ParentResponse> callback) {
        System.out.println("Родитель логинится...");
        apiService.loginParent(buildToken(username, password)).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ParentResponse> call, Response<ParentResponse> response) {
                System.out.println(response);
                System.out.println(response.code());
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                    saveSession(username, password, response.body().getId(), LoginContract.RoleUser.PARENT, response.body().getFio());
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
    public void loginKid(String username, String password, ModelCallback<KidResponse> callback) {
        apiService.loginKid(buildToken(username, password)).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<KidResponse> call, Response<KidResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                    saveSession(username, password, response.body().getId(), LoginContract.RoleUser.KID, response.body().getFio());
                }
                else if (response.code() == 404 || response.code() == 401) {
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

    @Override
    public void registerParent(ParentRegisterRequest request, ModelCallback<ParentResponse> callback) {
        apiService.registerParent(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ParentResponse> call, Response<ParentResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                    saveSession(request.getEmail(), request.getPassword(), response.body().getId(), LoginContract.RoleUser.PARENT, response.body().getFio());
                }
                else if (response.code() == 400) {
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
    public boolean isLoggedIn() {
        return localStorage.getBoolean(KEY_LOGGED_IN, false);
    }

    @Override
    public LoginContract.RoleUser getRole() {
        String getFromStorageRole = localStorage.getString(KEY_ROLE, "");
        if (getFromStorageRole.isEmpty())
            return null;
        return LoginContract.RoleUser.valueOf(getFromStorageRole);
    }

    @Override
    public void saveSession(String username, String password, long userId, LoginContract.RoleUser role, String fio) {
        localStorage.putString(KEY_USERNAME, username);
        localStorage.putString(KEY_TOKEN, buildToken(username, password));
        localStorage.putBoolean(KEY_LOGGED_IN, true);
        localStorage.putString(KEY_ROLE, role.name());
        localStorage.putString(KEY_PASSWORD, password);
        localStorage.putString(KEY_FIO, fio);
        localStorage.putString(KEY_USER_ID, String.valueOf(userId));
    }
}
