package com.lupinemoon.favicoin.presentation.ui.features.landing.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.data.models.Coins;
import com.lupinemoon.favicoin.data.storage.AppRepository;
import com.lupinemoon.favicoin.presentation.ui.base.BasePresenter;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;
import com.lupinemoon.favicoin.presentation.utils.Constants;
import com.lupinemoon.favicoin.presentation.utils.DialogUtils;
import com.lupinemoon.favicoin.presentation.utils.ErrorUtils;
import com.lupinemoon.favicoin.presentation.utils.NetworkUtils;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.GenericCallback;

import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmList;
import timber.log.Timber;

class HomePresenter extends BasePresenter implements HomeContract.Presenter {

    private HomeContract.View homeView;

    private AppRepository appRepository = AppRepository.getInstance();

    HomePresenter(@NonNull HomeContract.View view, @NonNull Bundle args) {
        // Set the view locally
        homeView = ActivityUtils.checkNotNull(view, "view cannot be null!");
    }

    @Override
    public void fetchCoinItems(long delay) {
        if (homeView.isAttached()) {
            homeView.showLoading();

            nonViewDisposables.add(
                    appRepository.getCoins(
                            homeView.getActivity().getApplicationContext(), 100)
                            .delay(delay > 0 ? delay : 0, TimeUnit.MILLISECONDS)
                            .concatMap(new Function<Coins, Publisher<Coins>>() {
                                @Override
                                public Publisher<Coins> apply(@io.reactivex.annotations.NonNull Coins coins) {
                                    return Flowable.just(manipulateCoinItems(coins));
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    new Consumer<Coins>() {
                                        @Override
                                        public void accept(Coins coins) {
                                            Timber.d("Observer Thread: %s", Thread.currentThread());
                                            if (homeView.isAttached()) {

                                                if (!TextUtils.isEmpty(coins.getSource()) &&
                                                        coins.getSource().equals(Constants.SOURCE_REALM) &&
                                                        coins.getCoinItems() != null &&
                                                        coins.getCoinItems().size() < 1 &&
                                                        NetworkUtils.isConnected(homeView.getActivity().getApplicationContext())
                                                        && homeView.getCoinItems().size() < 1) {
                                                    // Our local is empty and we are still fetching the network result
                                                    return;
                                                }

                                                homeView.hideLoading();

                                                if (homeView.getCoinItems().size() < 1) {
                                                    homeView.setCoinItems(coins.getCoinItems());
                                                } else if (coins.getCoinItems() != null && coins.getCoinItems().size() > 0) {
                                                    homeView.addCoinItems(coins.getCoinItems());
                                                }
                                            }
                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) {
                                            if (homeView.isAttached()) {
                                                homeView.hideLoading();

                                                if (!homeView.notAuthorized(throwable)) {
                                                    if (NetworkUtils.hasActiveNetworkConnection(
                                                            homeView.getActivity())) {
                                                        String message = ErrorUtils.getErrorMessage(
                                                                throwable,
                                                                homeView.getActivity().getApplicationContext());
                                                        homeView.showCustomAlertDialogSimple(
                                                                homeView.getActivity().getString(
                                                                        R.string.title_coins_failed),
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
                                                                        fetchCoinItems(0);
                                                                    }
                                                                },
                                                                DialogUtils.AlertType.NONE);
                                                    } else {
                                                        boolean offlineData = homeView.getCoinItems() != null && homeView.getCoinItems().size() > 0;
                                                        Timber.d("offlineData: %b", offlineData);
                                                        if (!offlineData) {
                                                            homeView.showNetworkErrorLayout(
                                                                    new GenericCallback() {
                                                                        @Override
                                                                        public void execute() {
                                                                            fetchCoinItems(0);
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }));
        }
    }

    private Coins manipulateCoinItems(Coins coins) {
        if (coins != null && coins.getCoinItems() != null) {
            Timber.d("Manipulate Coin Items: %s", coins.getCoinItems());
            Timber.d("Transform Thread: %s", Thread.currentThread());

            // Remove any duplicates
            if (homeView.getCoinItems() != null && homeView.getCoinItems().size() > 0) {
                RealmList<CoinItem> results = new RealmList<>();
                for (CoinItem coinItem2 : coins.getCoinItems()) {
                    boolean found = false;
                    for (CoinItem coinItem1 : homeView.getCoinItems()) {
                        if (coinItem2.getId().equals(
                                coinItem1.getId())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // Add new items
                        results.add(coinItem2);
                    }
                }
                Timber.d("coin items: %s", results);
                coins.getCoinItems().clear();
                coins.setCoinItems(results);
            }
        }
        return coins;
    }
}
