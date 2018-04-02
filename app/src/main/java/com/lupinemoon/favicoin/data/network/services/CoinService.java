package com.lupinemoon.favicoin.data.network.services;

import android.content.Context;

import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.data.models.Coins;
import com.lupinemoon.favicoin.data.network.interfaces.CoinApi;
import com.lupinemoon.favicoin.data.network.rest.ServiceGenerator;
import com.lupinemoon.favicoin.data.storage.AppRepository;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.realm.RealmList;

public class CoinService {

    private static CoinService coinService;

    private CoinApi coinApi;

    private AppRepository appRepository;

    private CoinService(AppRepository appRepository) {
        this.appRepository = appRepository;
    }

    public static synchronized CoinService getInstance(AppRepository appRepository) {
        if (coinService == null) {
            coinService = new CoinService(appRepository);
        }
        return coinService;
    }

    private synchronized CoinApi getCoinApi(Context context) {
        // We want to reuse the same instance
        if (coinApi == null) {
            coinApi = ServiceGenerator.getInstance().createService(CoinApi.class, context);
        }

        return coinApi;
    }

    public Flowable<Coins> getCoins(Context context, int start, int limit) {
        return getCoinApi(context).getCoins(start, limit).map(new Function<List<CoinItem>, Coins>() {
            @Override
            public Coins apply(@NonNull List<CoinItem> coinItems) {
                Coins coins = new Coins();
                RealmList<CoinItem> realmList = new RealmList<>();
                realmList.addAll(coinItems);
                coins.setCoinItems(realmList);
                return coins;
            }
        });
    }
}
