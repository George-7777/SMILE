package com.gvayt.smile.contract;


import com.gvayt.smile.model.network.dto.KidResponse;
import com.gvayt.smile.model.network.dto.ParentResponse;

import java.util.List;

public interface ParentContract {
    interface View {
        void showParentInfo(String fio);
        void showNetworkError();
        void showKidsList(List<KidResponse> kidsList);
        void showRegistrationDialog();
        void showTasksDialog(String usernameKid);
        void exitActivity();
    }
    interface Presenter {
        void onViewCreate();
        void onButtonAddKidClick();
        void onKidClick(String usernameKid);
        void onButtonLogoutClick();
    }
    interface Model {
        void getParentInfo(LoginContract.ModelCallback<ParentResponse> callback);
        void logout();
    }
}
