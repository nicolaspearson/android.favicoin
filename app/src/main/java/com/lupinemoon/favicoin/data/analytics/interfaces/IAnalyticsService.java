package com.lupinemoon.favicoin.data.analytics.interfaces;


import com.lupinemoon.favicoin.data.models.CoinItem;

public interface IAnalyticsService {

    void onViewStart(String viewResourceName);

    void reportPlayServicesVersion(String playServicesVersion);

    void reportAndroidOsVersion(String androidOsVersion);

    void reportCoinItemFavourited(CoinItem coinItem);
}
