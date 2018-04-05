package com.lupinemoon.favicoin.presentation.ui.features.coindetail;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.lupinemoon.favicoin.R;
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

    CoinDetailPresenter(@NonNull CoinDetailContract.View view, @NonNull Bundle args) {
        // Set the view locally
        coinDetailView = ActivityUtils.checkNotNull(view, "view cannot be null!");
    }

    @Override
    public void toggleFavourite(final CoinItem coinItem) {
        if (coinDetailView.isAttached()) {
            coinDetailView.showLoading();

            nonViewDisposables.add(
                    appRepository.toggleFavourite(coinItem, !coinItem.isFavourite())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    new Consumer<CoinItem>() {
                                        @Override
                                        public void accept(CoinItem updatedCoinItem) {
                                            Timber.d(
                                                    "Observer Thread: %s",
                                                    Thread.currentThread());
                                            if (coinDetailView.isAttached()) {
                                                coinDetailView.hideLoading();
                                                coinDetailView.favouriteToggleSuccess(updatedCoinItem);
                                            }
                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) {
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
                                                            new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    // Auto-dismissed
                                                                }
                                                            },
                                                            new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    toggleFavourite(coinItem);
                                                                }
                                                            },
                                                            DialogUtils.AlertType.NONE);
                                                }
                                            }
                                        }
                                    }));

        }
    }
}
