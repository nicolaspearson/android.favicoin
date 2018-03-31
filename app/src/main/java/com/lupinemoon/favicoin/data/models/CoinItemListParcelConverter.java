package com.lupinemoon.favicoin.data.models;

import org.parceler.ParcelConverter;
import org.parceler.Parcels;

import io.realm.RealmList;

public class CoinItemListParcelConverter implements ParcelConverter<RealmList<CoinItem>> {

    @Override
    public void toParcel(RealmList<CoinItem> input, android.os.Parcel parcel) {
        if (input == null) {
            parcel.writeInt(-1);
        } else {
            parcel.writeInt(input.size());
            for (CoinItem item : input) {
                parcel.writeParcelable(Parcels.wrap(item), 0);
            }
        }
    }

    @Override
    public RealmList<CoinItem> fromParcel(android.os.Parcel parcel) {
        int size = parcel.readInt();
        if (size < 0) {
            return null;
        }
        RealmList<CoinItem> items = new RealmList<>();
        for (int i = 0; i < size; ++i) {
            items.add((CoinItem) Parcels.unwrap(parcel.readParcelable(CoinItem.class.getClassLoader())));
        }
        return items;
    }
}
