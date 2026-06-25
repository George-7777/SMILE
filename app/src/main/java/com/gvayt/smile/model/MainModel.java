package com.gvayt.smile.model;

import com.gvayt.smile.Constant;
import com.gvayt.smile.contract.MainContract;
import com.gvayt.smile.model.local.LocalStorage;
import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.dto.kid.KidResponse;

import java.util.ArrayList;

public class MainModel implements MainContract.Model {
    private final LocalStorage localStorage;

    public MainModel(LocalStorage localStorage) {
        this.localStorage = localStorage;
    }

    @Override
    public void getKidInfo(ModelCallback<KidResponse> callback) {
        callback.onSuccess(new KidResponse(
                localStorage.getInt(Constant.KEY_USER_ID, -1),
                localStorage.getString(Constant.KEY_FIO, ""),
                localStorage.getString(Constant.KEY_USERNAME, ""),
                -1,
                new ArrayList<>()
        ));
    }

    @Override
    public void logout() {
        localStorage.clear();
    }
}
