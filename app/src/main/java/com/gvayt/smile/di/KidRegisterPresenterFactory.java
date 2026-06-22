package com.gvayt.smile.di;

import android.content.Context;

import com.gvayt.smile.contract.KidRegisterContract;
import com.gvayt.smile.model.KidRegisterModel;
import com.gvayt.smile.model.local.LocalStorage;
import com.gvayt.smile.model.local.SharedPrefStorage;
import com.gvayt.smile.model.network.ApiService;
import com.gvayt.smile.model.network.RetrofitClient;
import com.gvayt.smile.presenter.KidRegisterPresenter;

public class KidRegisterPresenterFactory {
    public static KidRegisterContract.Presenter create(KidRegisterContract.View view, Context context) {
        LocalStorage localStorage = new SharedPrefStorage(context);
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        KidRegisterContract.Model model = new KidRegisterModel(apiService, localStorage);
        return new KidRegisterPresenter(view, model);
    }
}
