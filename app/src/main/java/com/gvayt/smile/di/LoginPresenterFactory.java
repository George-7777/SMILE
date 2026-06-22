package com.gvayt.smile.di;

import android.content.Context;

import com.gvayt.smile.contract.LoginContract;
import com.gvayt.smile.model.LoginModel;
import com.gvayt.smile.model.local.LocalStorage;
import com.gvayt.smile.model.local.SharedPrefStorage;
import com.gvayt.smile.model.network.ApiService;
import com.gvayt.smile.model.network.RetrofitClient;
import com.gvayt.smile.presenter.LoginPresenter;

public class LoginPresenterFactory {
    public static LoginContract.Presenter create(LoginContract.View loginView, Context context) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        LocalStorage localStorage = new SharedPrefStorage(context);
        LoginContract.Model loginModel = new LoginModel(apiService, localStorage);
        return new LoginPresenter(loginView, loginModel);
    }
}
