package com.lupinemoon.favicoin.presentation.ui.features.splash;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lupinemoon.favicoin.presentation.ui.base.BasePresenter;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;

class SplashPresenter extends BasePresenter implements SplashContract.Presenter {

    private SplashContract.View splashView;

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

}
