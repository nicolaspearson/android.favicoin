package com.lupinemoon.favicoin.data.storage.remote;

import android.content.Context;

import com.lupinemoon.favicoin.data.models.AuthToken;
import com.lupinemoon.favicoin.data.models.Coins;
import com.lupinemoon.favicoin.data.models.KeyValue;
import com.lupinemoon.favicoin.data.network.services.AuthService;
import com.lupinemoon.favicoin.data.network.services.CoinService;
import com.lupinemoon.favicoin.data.network.services.TemplateService;
import com.lupinemoon.favicoin.data.storage.AppRepository;
import com.lupinemoon.favicoin.data.storage.interfaces.AppDataStore;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class AppRemoteDataStore implements AppDataStore {

    private static AppRemoteDataStore appRemoteDataStore;

    private AuthService authService;
    private CoinService coinService;
    private TemplateService templateService;

    private AppRemoteDataStore(AppRepository appRepository) {
        authService = AuthService.getInstance();
        coinService = CoinService.getInstance(appRepository);
        templateService = TemplateService.getInstance(appRepository);
    }

    public static synchronized AppRemoteDataStore getInstance(AppRepository appRepository) {
        if (appRemoteDataStore == null) {
            appRemoteDataStore = new AppRemoteDataStore(appRepository);
        }
        return appRemoteDataStore;
    }

    // region Auth API
    @Override
    public Flowable<AuthToken> doLogin(
            Context context, String msisdn, String password, String websiteId) {
        return authService.doLogin(context, msisdn, password, websiteId);
    }
    // endregion

    // region Template API
    @Override
    public Completable performNotifyApiCall(Context context) {
        return templateService.doNotifyApiCall(context);
    }

    @Override
    public Flowable<KeyValue> fetchKeyValue(Context context, String key) {
        return templateService.getKeyValue(context, key);
    }

    @Override
    public Flowable<KeyValue> saveKeyValue(Context context, KeyValue keyValue) {
        return templateService.postKeyValue(context, keyValue);
    }
    // endregion

    // region Coins API
    @Override
    public Flowable<Coins> getCoins(Context context, int limit) {
        return coinService.getCoins(context, limit);
    }
    // endregion
}
