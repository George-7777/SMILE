package com.gvayt.smile.presenter;

import com.gvayt.smile.contract.KidRegisterContract;
import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.TypeApiError;
import com.gvayt.smile.model.network.dto.kid.KidRegisterRequest;
import com.gvayt.smile.model.network.dto.kid.KidResponse;

public class KidRegisterPresenter implements KidRegisterContract.Presenter {
    private final KidRegisterContract.View view;
    private final KidRegisterContract.Model model;

    public KidRegisterPresenter(KidRegisterContract.View view, KidRegisterContract.Model model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void onButtonRegisterClick(String username, String fio, String password) {
        model.registerKid(new KidRegisterRequest(fio, username, password), new ModelCallback<>() {
            @Override
            public void onSuccess(KidResponse result) {
                view.showRegisterSuccess();
                view.returnToParentActivity();
            }

            @Override
            public void onError(TypeApiError error) {
                switch (error) {
                    case SERVER:
                        view.showServerError();
                        break;
                    case CLIENT:
                        view.showRegisterFailed();
                        break;
                    case NETWORK:
                        view.showNetworkError();
                        break;
                }
            }
        });
    }

    @Override
    public void onButtonReturnClick() {
        view.returnToParentActivity();
    }
}
