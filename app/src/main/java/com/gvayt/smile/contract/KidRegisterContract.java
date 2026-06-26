package com.gvayt.smile.contract;

import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.dto.kid.KidRegisterRequest;
import com.gvayt.smile.model.network.dto.kid.KidResponse;

public interface KidRegisterContract {
    interface View {
        void showRegisterSuccess();
        void showRegisterFailed();
        void showServerError();
        void showNetworkError();
        void returnToParentActivity();
    }
    interface Presenter {
        void onButtonRegisterClick(String username, String fio, String password);
        void onButtonReturnClick();
    }
    interface Model {
        void registerKid(KidRegisterRequest kidRegisterRequest, ModelCallback<KidResponse> callback);
    }
}
