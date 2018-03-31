package com.lupinemoon.favicoin.data.models;

import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;

import io.realm.RealmList;
import lombok.ToString;

@ToString
@Parcel
@SuppressWarnings("unused, WeakerAccess")
public class Coins {
    @ParcelPropertyConverter(CoinItemListParcelConverter.class)
    RealmList<CoinItem> coinItems = new RealmList<>();

    String source;

    public Coins() {
        // Empty constructor needed by the Parceler library
    }

    public RealmList<CoinItem> getCoinItems() {
        return coinItems;
    }

    public void setCoinItems(RealmList<CoinItem> coinItems) {
        this.coinItems = coinItems;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
