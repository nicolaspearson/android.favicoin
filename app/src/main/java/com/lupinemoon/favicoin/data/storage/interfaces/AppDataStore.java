package com.lupinemoon.favicoin.data.storage.interfaces;

import android.content.Context;

import com.lupinemoon.favicoin.data.models.AuthToken;
import com.lupinemoon.favicoin.data.models.KeyValue;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public interface AppDataStore {

    // Template API
    Completable performNotifyApiCall(Context context);
    Flowable<KeyValue> fetchKeyValue(Context context, String key);
    Flowable<KeyValue> saveKeyValue(Context context, KeyValue keyValue);

    // Auth API
    Flowable<AuthToken> doLogin(Context context, String msisdn, String password, String websiteId);
}
