package com.lupinemoon.favicoin.data.network.interfaces;

import com.lupinemoon.favicoin.data.models.CoinItem;

import java.util.List;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CoinApi {

    @GET("ticker")
    Flowable<List<CoinItem>> getCoins(
            @Query("limit") int limit);

}
