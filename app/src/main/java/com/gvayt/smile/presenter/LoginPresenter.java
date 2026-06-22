package com.gvayt.smile.presenter;

import android.content.Context;

import com.gvayt.smile.contract.LoginContract;
import com.gvayt.smile.model.LoginModel;
import com.gvayt.smile.model.network.ApiService;
import com.gvayt.smile.model.network.RetrofitClient;
import com.gvayt.smile.model.network.dto.KidResponse;
import com.gvayt.smile.model.network.dto.ParentResponse;
import com.gvayt.smile.model.network.dto.ParentRegisterRequest;


public class LoginPresenter implements LoginContract.Presenter {
    private final LoginContract.View loginView;
    private final LoginContract.Model loginModel;

    public LoginPresenter(LoginContract.View loginView, LoginContract.Model loginModel) {
        this.loginView = loginView;
        this.loginModel = loginModel;
    }

    // =======================IMPL=======================

    @Override
    public void onButtonLoginClick(String login, String password, LoginContract.RoleUser role) {
        if (role.equals(LoginContract.RoleUser.KID)) {
            loginModel.loginKid(login, password, new LoginContract.ModelCallback<>() {
                @Override
                public void onSuccess(KidResponse result) {
                    loginView.showLoginSuccess();
                    loginView.redirectToKidActivity();
                }

                @Override
                public void onError(LoginContract.LoginError error) {
                    if (error.equals(LoginContract.LoginError.SERVER)) {
                        loginView.showServerError();
                    } else if (error.equals(LoginContract.LoginError.CLIENT)) {
                        loginView.showLoginFailed();
                    } else {
                        loginView.showNetworkError();
                    }
                }
            });
        }
        else {
            loginModel.loginParent(login, password, new LoginContract.ModelCallback<>() {
                @Override
                public void onSuccess(ParentResponse result) {
                    loginView.showLoginSuccess();
                    loginView.redirectToParentActivity();
                }

                @Override
                public void onError(LoginContract.LoginError error) {
                    if (error.equals(LoginContract.LoginError.SERVER)) {
                        loginView.showServerError();
                    } else if (error.equals(LoginContract.LoginError.CLIENT)) {
                        loginView.showLoginFailed();
                    } else {
                        loginView.showNetworkError();
                    }
                }
            });
        }
    }

    @Override
    public void onButtonRegisterClick(String email, String fio, String password) {
        loginModel.registerParent(new ParentRegisterRequest(fio, email, password), new LoginContract.ModelCallback<>() {

            @Override
            public void onSuccess(ParentResponse result) {
                loginView.showRegisterSuccess();
                loginView.redirectToParentActivity();
            }

            @Override
            public void onError(LoginContract.LoginError error) {
                if (error.equals(LoginContract.LoginError.SERVER)) {
                    loginView.showServerError();
                } else if (error.equals(LoginContract.LoginError.CLIENT)) {
                    loginView.showRegisterFailed();
                } else {
                    loginView.showNetworkError();
                }
            }
        });
    }

    @Override
    public void onButtonAnonClick() {
        loginView.redirectToAnonKid();
    }

    @Override
    public void onViewCreated() {
        if (loginModel.isLoggedIn()) {
            if (loginModel.getRole().equals(LoginContract.RoleUser.KID)) {
                loginView.redirectToKidActivity();
            }
            else {
                loginView.redirectToParentActivity();
            }
        }
    }

    @Override
    public void onSwitchMode(boolean toLogin) {
        if (toLogin) loginView.switchToLoginMode();
        else loginView.switchToRegisterMode();
    }
}
