package com.lupinemoon.favicoin.presentation.ui.features.splash;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lupinemoon.favicoin.data.storage.AppRepository;
import com.lupinemoon.favicoin.presentation.ui.base.BasePresenter;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

class SplashPresenter extends BasePresenter implements SplashContract.Presenter {

    private SplashContract.View splashView;

    private AppRepository appRepository = AppRepository.getInstance();

    SplashPresenter(@NonNull SplashContract.View view, @Nullable Bundle args) {
        // Set the view locally
        splashView = ActivityUtils.checkNotNull(view, "view cannot be null!");
    }

    @Override
    public void performAutoLogin() {
        if (splashView.isAttached()) {
            splashView.startLandingActivity();
        }
    }

    @Override
    public void loadCryptoCompareCoins() {
        nonViewDisposables.add(
                appRepository.loadCryptoCompareCoins(
                        splashView.getActivity().getApplicationContext())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete(
                                new Action() {
                                    @Override
                                    public void run() {
                                        splashView.cryptoCompareCoinsLoaded();
                                    }
                                })
                        .subscribe());
    }

}
