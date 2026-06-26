package com.gvayt.smile.presenter;

import com.gvayt.smile.contract.ParentContract;
import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.TypeApiError;
import com.gvayt.smile.model.network.dto.parent.ParentResponse;

public class ParentPresenter implements ParentContract.Presenter {
    private ParentContract.View parentView;
    private ParentContract.Model parentModel;

    public ParentPresenter(ParentContract.View parentView, ParentContract.Model parentModel) {
        this.parentView = parentView;
        this.parentModel = parentModel;
    }
    @Override
    public void onViewCreate() {
        parentModel.getParentInfo(new ModelCallback<>() {
            @Override
            public void onSuccess(ParentResponse result) {
                parentView.showParentInfo(result.getFio());
                parentView.showKidsList(result.getKidList());
            }

            @Override
            public void onError(TypeApiError error) {
                parentView.showNetworkError();
            }
        });
    }

    @Override
    public void onButtonAddKidClick() {
        parentView.showRegistrationDialog();
    }

    @Override
    public void onKidClick(String usernameKid) {
        parentView.showTasksDialog(usernameKid);
    }

    @Override
    public void onButtonLogoutClick() {
        parentModel.logout();
        parentView.exitActivity();
    }
}
