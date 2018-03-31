package com.lupinemoon.favicoin.presentation.services.rxbus.events;

import com.lupinemoon.favicoin.data.models.Coins;

public class UpdatedCoinsEvent {
    private Coins coins;

    public UpdatedCoinsEvent(Coins coins) {
        this.coins = coins;
    }

    public Coins getCoins() {
        return coins;
    }
}
