package com.gvayt.smile.contract;

import com.gvayt.smile.model.network.dto.KidResponse;
import com.gvayt.smile.model.network.dto.ParentResponse;
import com.gvayt.smile.model.network.dto.ParentRegisterRequest;

public interface LoginContract {
    interface View {
        void showLoginSuccess();
        void showLoginFailed();
        void showRegisterSuccess();
        void showRegisterFailed();
        void showServerError();
        void showNetworkError();
        void switchToRegisterMode();
        void switchToLoginMode();
        void redirectToParentActivity();
        void redirectToKidActivity();
        void redirectToAnonKid();
    }
    interface Presenter {
        void onButtonLoginClick(String login, String password, RoleUser role);
        void onButtonRegisterClick(String email, String fio, String password);
        void onButtonAnonClick();
        void onViewCreated();
        void onSwitchMode(boolean toLogin);
    }
    interface Model {
        void loginParent(String username, String password, ModelCallback<ParentResponse> callback);
        void loginKid(String username, String password, ModelCallback<KidResponse> callback);
        void registerParent(ParentRegisterRequest request, ModelCallback<ParentResponse> callback);
        boolean isLoggedIn();
        RoleUser getRole();
        String getToken();
        void saveSession(String username, String password, long userId, RoleUser role, String fio);
    }
    interface ModelCallback<T> {
        void onSuccess(T result);
        void onError(LoginError error);
    }
    enum RoleUser {
        KID,
        PARENT
    }
    enum LoginError {
        SERVER,
        CLIENT,
        NETWORK
    }
}
