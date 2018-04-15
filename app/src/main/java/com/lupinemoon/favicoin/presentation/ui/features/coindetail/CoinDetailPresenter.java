package com.lupinemoon.favicoin.presentation.ui.features.coindetail;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.data.analytics.AnalyticsService;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.data.storage.AppRepository;
import com.lupinemoon.favicoin.presentation.ui.base.BasePresenter;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;
import com.lupinemoon.favicoin.presentation.utils.DialogUtils;
import com.lupinemoon.favicoin.presentation.utils.ErrorUtils;
import com.lupinemoon.favicoin.presentation.utils.NetworkUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

class CoinDetailPresenter extends BasePresenter implements CoinDetailContract.Presenter {

    private CoinDetailContract.View coinDetailView;

    private AppRepository appRepository = AppRepository.getInstance();

    private AnalyticsService analyticsService = AnalyticsService.getInstance();

    CoinDetailPresenter(@NonNull CoinDetailContract.View view, @NonNull Bundle args) {
        // Set the view locally
        coinDetailView = ActivityUtils.checkNotNull(view, "view cannot be null!");
    }

    @Override
    public void toggleFavourite(final CoinItem coinItem) {
        if (coinDetailView.isAttached()) {
            coinDetailView.showLoading();

            if (coinItem.isFavourite()) {
                analyticsService.reportCoinItemFavourited(coinItem);
            }

            nonViewDisposables.add(
                    appRepository.toggleFavourite(coinItem, !coinItem.isFavourite())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    updatedCoinItem -> {
                                        Timber.d(
                                                "Observer Thread: %s",
                                                Thread.currentThread());
                                        if (coinDetailView.isAttached()) {
                                            coinDetailView.hideLoading();
                                            coinDetailView.favouriteToggleSuccess(updatedCoinItem);
                                        }
                                    }, throwable -> {
                                        if (coinDetailView.isAttached()) {
                                            coinDetailView.hideLoading();

                                            if (NetworkUtils.hasActiveNetworkConnection(
                                                    coinDetailView.getActivity())) {
                                                String message = ErrorUtils.getErrorMessage(
                                                        throwable,
                                                        coinDetailView.getActivity().getApplicationContext());
                                                coinDetailView.showCustomAlertDialogSimple(
                                                        coinDetailView.getActivity().getString(
                                                                R.string.title_favourite_failed),
                                                        message,
                                                        R.string.cancel,
                                                        R.string.retry,
                                                        view -> {
                                                            // Auto-dismissed
                                                        },
                                                        view -> toggleFavourite(coinItem),
                                                        DialogUtils.AlertType.NONE);
                                            }
                                        }
                                    }));

        }
    }
}
