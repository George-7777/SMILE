package com.gvayt.smile.model;

import static com.gvayt.smile.Constant.KEY_FIO;
import static com.gvayt.smile.Constant.KEY_LOGGED_IN;
import static com.gvayt.smile.Constant.KEY_PASSWORD;
import static com.gvayt.smile.Constant.KEY_ROLE;
import static com.gvayt.smile.Constant.KEY_TOKEN;
import static com.gvayt.smile.Constant.KEY_USERNAME;
import static com.gvayt.smile.Constant.KEY_USER_ID;
import static com.gvayt.smile.Constant.PREF_NAME;

import android.content.Context;
import android.content.SharedPreferences;

import com.gvayt.smile.contract.LoginContract;
import com.gvayt.smile.model.network.ApiService;
import com.gvayt.smile.model.network.dto.KidLoginResponse;
import com.gvayt.smile.model.network.dto.ParentLoginResponse;
import com.gvayt.smile.model.network.dto.ParentRegisterRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.Base64;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Регистрация/вход через Basic auth и хранение в SharedPreferences
public class LoginModel implements LoginContract.Model {
    private static final Logger log = LoggerFactory.getLogger(LoginModel.class);
    private ApiService apiService;
    private final SharedPreferences sharedPreferences;

    public LoginModel(ApiService apiService, Context context) {
        this.apiService = apiService;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    private String buildToken(String username, String password) {
        return Credentials.basic(username, password);
    }
    @Override
    public void loginParent(String username, String password, LoginContract.ModelCallback<ParentLoginResponse> callback) {
        System.out.println("Родитель логинится...");
        apiService.loginParent(buildToken(username, password)).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ParentLoginResponse> call, Response<ParentLoginResponse> response) {
                System.out.println(response);
                System.out.println(response.code());
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                    saveSession(username, password, response.body().getId(), LoginContract.RoleUser.PARENT, response.body().getFio());
                }
                else if (response.code() == 404) {
                    callback.onError(LoginContract.LoginError.CLIENT);
                }
                else {
                    callback.onError(LoginContract.LoginError.SERVER);
                }
            }

            @Override
            public void onFailure(Call<ParentLoginResponse> call, Throwable t) {
                callback.onError(LoginContract.LoginError.NETWORK);
            }
        });
    }

    @Override
    public void loginKid(String username, String password, LoginContract.ModelCallback<KidLoginResponse> callback) {
        apiService.loginKid(buildToken(username, password)).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<KidLoginResponse> call, Response<KidLoginResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                    saveSession(username, password, response.body().getId(), LoginContract.RoleUser.KID, response.body().getFio());
                }
                else if (response.code() == 404) {
                    callback.onError(LoginContract.LoginError.CLIENT);
                }
                else {
                    callback.onError(LoginContract.LoginError.SERVER);
                }
            }

            @Override
            public void onFailure(Call<KidLoginResponse> call, Throwable t) {
                callback.onError(LoginContract.LoginError.NETWORK);
            }
        });
    }

    @Override
    public void registerParent(ParentRegisterRequest request, LoginContract.ModelCallback<ParentLoginResponse> callback) {
        apiService.registerParent(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ParentLoginResponse> call, Response<ParentLoginResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                    saveSession(request.getEmail(), request.getPassword(), response.body().getId(), LoginContract.RoleUser.PARENT, response.body().getFio());
                }
                else if (response.code() == 400) {
                    callback.onError(LoginContract.LoginError.CLIENT);
                }
                else {
                    callback.onError(LoginContract.LoginError.SERVER);
                }
            }

            @Override
            public void onFailure(Call<ParentLoginResponse> call, Throwable t) {
                callback.onError(LoginContract.LoginError.NETWORK);
            }
        });
    }

    @Override
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    @Override
    public LoginContract.RoleUser getRole() {
        String getFromStorageRole = sharedPreferences.getString(KEY_ROLE, "");
        if (getFromStorageRole.isEmpty())
            return null;
        return LoginContract.RoleUser.valueOf(getFromStorageRole);
    }

    @Override
    public String getToken() {
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        String password = sharedPreferences.getString(KEY_PASSWORD, "");
        if (username.isEmpty() || password.isEmpty())
            return null;
        else
            return buildToken(username, password);
    }

    @Override
    public void saveSession(String username, String password, long userId, LoginContract.RoleUser role, String fio) {
        sharedPreferences.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_TOKEN, buildToken(username, password))
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_ROLE, role.name())
                .putString(KEY_PASSWORD, password)
                .putString(KEY_FIO, fio)
                .putString(KEY_USER_ID, String.valueOf(userId))
                .apply();
    }
}
