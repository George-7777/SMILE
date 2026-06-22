package com.gvayt.smile.di;

import android.content.Context;

import com.gvayt.smile.contract.ParentContract;
import com.gvayt.smile.model.ParentModel;
import com.gvayt.smile.model.local.LocalStorage;
import com.gvayt.smile.model.local.SharedPrefStorage;
import com.gvayt.smile.model.network.ApiService;
import com.gvayt.smile.model.network.RetrofitClient;
import com.gvayt.smile.presenter.ParentPresenter;

public class ParentPresenterFactory {
    public static ParentContract.Presenter create(ParentContract.View parentView, Context context) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        LocalStorage localStorage = new SharedPrefStorage(context);
        ParentContract.Model parentModel = new ParentModel(apiService, localStorage);
        return new ParentPresenter(parentView, parentModel);
    }
}
