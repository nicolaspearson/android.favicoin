package com.lupinemoon.favicoin.presentation.ui.features.landing.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.view.View;

import com.lupinemoon.favicoin.BuildConfig;
import com.lupinemoon.favicoin.MainApplication;
import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.data.models.Coins;
import com.lupinemoon.favicoin.data.storage.AppRepository;
import com.lupinemoon.favicoin.presentation.ui.base.BasePresenter;
import com.lupinemoon.favicoin.presentation.ui.features.coindetail.CoinDetailActivity;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;
import com.lupinemoon.favicoin.presentation.utils.Constants;
import com.lupinemoon.favicoin.presentation.utils.DialogUtils;
import com.lupinemoon.favicoin.presentation.utils.ErrorUtils;
import com.lupinemoon.favicoin.presentation.utils.NetworkUtils;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.GenericCallback;

import org.parceler.Parcels;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
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

    private ArrayList<CoinItem> coinItemList = new ArrayList<>();

    private int page = 1;
    private int maxPage = 1;
    private int totalCount = 0;
    private boolean busyFetching = false;

    HomePresenter(@NonNull HomeContract.View view, @NonNull Bundle args) {
        // Set the view locally
        homeView = ActivityUtils.checkNotNull(view, "view cannot be null!");
    }


    @Override
    public void updateCoinItems(Coins coins) {
        if (coins != null) {
            // coinItemList.clear();
            coinItemList.addAll(coins.getCoinItems());
            homeView.setCoinItems(coinItemList, true);
        }
    }

    @Override
    public void fetchCoinItems(final boolean refresh, long delay) {
        if (homeView.isAttached()) {
            homeView.showLoading();

            if (refresh) {
                coinItemList.clear();
                page = 1;
            }

            busyFetching = true;

            nonViewDisposables.add(
                    appRepository.getCoins(
                            homeView.getActivity().getApplicationContext(),
                            BuildConfig.COIN_PAGE_SIZE * (page - 1),
                            BuildConfig.COIN_PAGE_SIZE)
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
                                            busyFetching = false;
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
                                                    homeView.setCoinItems(coinItemList, refresh);
                                                } else if (coins.getCoinItems() != null && coins.getCoinItems().size() > 0) {
                                                    homeView.addCoinItems(
                                                            coins.getCoinItems());
                                                }
                                            }
                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) {
                                            busyFetching = false;
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
                                                                        fetchCoinItems(refresh, 0);
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
                                                                            fetchCoinItems(
                                                                                    refresh,
                                                                                    0);
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

    @Override
    public void showCoinDetailView(
            CoinItem coinItem, View transitionView, Bitmap imageBitmap) {
        if (homeView.isAttached()) {
            Intent intent = new Intent(homeView.getActivity(), CoinDetailActivity.class);
            intent.putExtra(Constants.INTENT_COIN_ITEM, Parcels.wrap(coinItem));

            // Do not make a scene transition if the user does not have a network connection
            // we do not know at this point if the profile to be loaded is available offline
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !NetworkUtils.hasActiveNetworkConnection(
                    homeView.getActivity().getApplicationContext())) {
                homeView.getActivity().startActivity(intent);
            } else {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        homeView.getActivity(),
                        transitionView,
                        homeView.getActivity().getResources().getString(R.string.shared_image));
                intent.putExtra(Constants.KEY_SHOULD_USE_CIRCLE, true);
                MainApplication.getBitmapCache().delBitmap();
                MainApplication.getBitmapCache().putBitmap(imageBitmap);
                homeView.getActivity().startActivity(intent, options.toBundle());
            }
        }
    }

    @Override
    public void loadMore(int itemCount) {
        if (totalCount <= 0 || busyFetching) {
            return;
        }
        if (page < maxPage && itemCount < totalCount) {
            page++;
            fetchCoinItems(false, 0);
            homeView.showSnackbarMsg(homeView.getActivity().getString(R.string.loading), 500);
        }
    }

    private Coins manipulateCoinItems(Coins coins) {
        Timber.d("Manipulate Coin Items: %s", coins.getCoinItems());
        Timber.d("Transform Thread: %s", Thread.currentThread());

        // The API does not provide a nice way to perform pagination
        totalCount = BuildConfig.MAX_NUM_COINS;
        maxPage = Math.round(totalCount / BuildConfig.COIN_PAGE_SIZE) + ((totalCount % BuildConfig.COIN_PAGE_SIZE) > 0 ? 1 : 0);

        Timber.d(
                "Page: %d | Page Size: %d | Max Page: %d | Total Count: %d",
                page,
                BuildConfig.COIN_PAGE_SIZE,
                maxPage,
                totalCount);

        // Remove any duplicates
        if (homeView.getCoinItems() != null && homeView.getCoinItems().size() > 0) {
            RealmList<CoinItem> results = new RealmList<>();
            for (CoinItem coinItem2 : coins.getCoinItems()) {
                boolean found = false;
                for (CoinItem coinItem1 : coinItemList) {
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
            Timber.d("coinItems: %s", results);
            coins.setCoinItems(results);
            coinItemList.addAll(results);
        } else {
            coinItemList = coins.getCoinItems() != null ? new ArrayList<>(coins.getCoinItems()) : new ArrayList<CoinItem>();
        }

        return coins;
    }
}
