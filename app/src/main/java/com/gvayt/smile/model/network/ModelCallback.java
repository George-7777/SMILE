package com.gvayt.smile.model.network;

public interface ModelCallback<T> {
    void onSuccess(T result);

    void onError(TypeApiError error);
}
