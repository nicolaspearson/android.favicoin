package com.lupinemoon.favicoin.data.storage.remote;

import android.content.Context;

import com.lupinemoon.favicoin.data.models.AuthToken;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.data.models.Coins;
import com.lupinemoon.favicoin.data.network.services.AuthService;
import com.lupinemoon.favicoin.data.network.services.CoinService;
import com.lupinemoon.favicoin.data.storage.AppRepository;
import com.lupinemoon.favicoin.data.storage.interfaces.AppDataStore;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class AppRemoteDataStore implements AppDataStore {

    private static AppRemoteDataStore appRemoteDataStore;

    private AuthService authService;
    private CoinService coinService;

    private AppRemoteDataStore(AppRepository appRepository) {
        authService = AuthService.getInstance();
        coinService = CoinService.getInstance(appRepository);
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
            Context context, String username, String password) {
        return authService.doLogin(context, username, password);
    }
    // endregion

    // region Coins API
    @Override
    public Flowable<Coins> getCoins(Context context, int start, int limit) {
        return coinService.getCoins(context, start, limit);
    }
    // endregion

    // region Favourites API
    @Override
    public Flowable<Coins> getFavourites() {
        // Local only
        return null;
    }

    @Override
    public Flowable<CoinItem> toggleFavourite(
            CoinItem coinItem, boolean isFavourite) {
        // Local only
        return null;
    }
    // endregion

    // region Crypto Compare API
    @Override
    public Completable loadCryptoCompareCoins(Context context) {
        // Local only
        return Completable.complete();
    }
    // endregion
}
