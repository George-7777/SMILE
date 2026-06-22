package com.gvayt.smile.contract;

import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.dto.kid.KidResponse;
import com.gvayt.smile.model.network.dto.parent.ParentResponse;
import com.gvayt.smile.model.network.dto.parent.ParentRegisterRequest;

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
        void onButtonRegisterClick(String login, String fio, String password);
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
        void saveSession(String username, String password, long userId, RoleUser role, String fio);
    }

    enum RoleUser {
        KID,
        PARENT
    }
}
