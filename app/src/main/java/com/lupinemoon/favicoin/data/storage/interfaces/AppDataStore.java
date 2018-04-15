package com.lupinemoon.favicoin.data.storage.interfaces;

import android.content.Context;

import com.lupinemoon.favicoin.data.models.AuthToken;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.data.models.Coins;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public interface AppDataStore {

    // Auth API
    Flowable<AuthToken> doLogin(Context context, String username, String password);

    // Coin API
    Flowable<Coins> getCoins(final Context context, int start, int limit);

    // Favourites API
    Flowable<Coins> getFavourites();

    Flowable<CoinItem> toggleFavourite(final CoinItem coinItem, boolean isFavourite);

    // Crypto Compare API
    Completable loadCryptoCompareCoins(Context context);
}
