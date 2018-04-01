package com.lupinemoon.favicoin.presentation.ui.features.coindetail;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import com.lupinemoon.favicoin.presentation.ui.base.BasePresenter;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;

class CoinDetailPresenter extends BasePresenter implements CoinDetailContract.Presenter {

    private CoinDetailContract.View coinDetailView;

    CoinDetailPresenter(@NonNull CoinDetailContract.View view, @NonNull Bundle args) {
        // Set the view locally
        coinDetailView = ActivityUtils.checkNotNull(view, "view cannot be null!");
    }

    @Override
    public void performAction(String actionText) {
        coinDetailView.showSnackbarMsg(actionText, Snackbar.LENGTH_SHORT);
    }

}
