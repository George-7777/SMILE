package com.gvayt.smile.di;

import android.content.Context;

import com.gvayt.smile.contract.ParentTasksContract;
import com.gvayt.smile.model.ParentTasksModel;
import com.gvayt.smile.model.local.LocalStorage;
import com.gvayt.smile.model.local.SharedPrefStorage;
import com.gvayt.smile.model.network.ApiService;
import com.gvayt.smile.model.network.RetrofitClient;
import com.gvayt.smile.presenter.ParentTasksPresenter;

public class ParentTasksPresenterFactory {
    public static ParentTasksContract.Presenter create(ParentTasksContract.View parentView, Context context) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        LocalStorage localStorage = new SharedPrefStorage(context);
        ParentTasksContract.Model parentModel = new ParentTasksModel(localStorage, apiService);
        return new ParentTasksPresenter(parentView, parentModel);
    }
}
