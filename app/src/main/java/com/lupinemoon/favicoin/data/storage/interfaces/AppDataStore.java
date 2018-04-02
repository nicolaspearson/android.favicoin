package com.lupinemoon.favicoin.data.storage.interfaces;

import android.content.Context;

import com.lupinemoon.favicoin.data.models.AuthToken;
import com.lupinemoon.favicoin.data.models.Coins;
import com.lupinemoon.favicoin.data.models.KeyValue;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public interface AppDataStore {

    // Template API
    Completable performNotifyApiCall(Context context);
    Flowable<KeyValue> fetchKeyValue(Context context, String key);
    Flowable<KeyValue> saveKeyValue(Context context, KeyValue keyValue);

    // Auth API
    Flowable<AuthToken> doLogin(Context context, String username, String password);

    // Coin API
    Flowable<Coins> getCoins(final Context context, int start, int limit);
}
