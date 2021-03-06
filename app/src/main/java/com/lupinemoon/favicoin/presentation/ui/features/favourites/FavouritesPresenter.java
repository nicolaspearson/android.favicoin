package com.lupinemoon.favicoin.presentation.ui.features.favourites;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.view.View;

import com.lupinemoon.favicoin.MainApplication;
import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.data.models.Coins;
import com.lupinemoon.favicoin.data.storage.AppRepository;
import com.lupinemoon.favicoin.presentation.ui.base.BasePresenter;
import com.lupinemoon.favicoin.presentation.ui.features.coindetail.CoinDetailActivity;
import com.lupinemoon.favicoin.presentation.ui.features.home.HomeFragment;
import com.lupinemoon.favicoin.presentation.ui.features.landing.LandingActivity;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;
import com.lupinemoon.favicoin.presentation.utils.Constants;
import com.lupinemoon.favicoin.presentation.utils.DialogUtils;
import com.lupinemoon.favicoin.presentation.utils.ErrorUtils;
import com.lupinemoon.favicoin.presentation.utils.NetworkUtils;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.GenericCallback;

import org.parceler.Parcels;
import org.reactivestreams.Publisher;

import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmList;
import timber.log.Timber;

class FavouritesPresenter extends BasePresenter implements FavouritesContract.Presenter {

    private FavouritesContract.View favouritesView;

    private AppRepository appRepository = AppRepository.getInstance();

    private RealmList<CoinItem> coinItemList = new RealmList<>();

    FavouritesPresenter(@NonNull FavouritesContract.View view, @NonNull Bundle args) {
        // Set the view locally
        favouritesView = ActivityUtils.checkNotNull(view, "view cannot be null!");
    }

    @Override
    public void performEmptyButtonAction() {
        if (favouritesView.isAttached()) {
            ((LandingActivity) favouritesView.getActivity()).selectMenuItem(HomeFragment.TAG, true);
        }
    }

    @Override
    public void removeCoinItem(CoinItem coinItem) {
        if (coinItem != null) {
            int position = -1;
            for (int i = 0; i < coinItemList.size(); i++) {
                CoinItem item = coinItemList.get(i);
                if (coinItem.getId().equals(item.getId())) {
                    position = i;
                    break;
                }
            }
            if (position >= 0) {
                coinItemList.remove(position);
            }
        }
    }

    @Override
    public void fetchCoinItems(final boolean refresh, long delay) {
        if (favouritesView.isAttached()) {
            favouritesView.showLoading();

            if (refresh) {
                coinItemList.clear();
                favouritesView.setCoinItems(coinItemList, true);
            }

            nonViewDisposables.add(
                    appRepository.getFavourites()
                            .delay(delay > 0 ? delay : 0, TimeUnit.MILLISECONDS)
                            .concatMap((Function<Coins, Publisher<Coins>>) coins -> Flowable.just(manipulateCoinItems(coins)))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    coins -> {
                                        Timber.d(
                                                "Observer Thread: %s",
                                                Thread.currentThread());
                                        if (favouritesView.isAttached()) {
                                            favouritesView.hideLoading();
                                            if (favouritesView.getCoinItems().size() < 1) {
                                                favouritesView.setCoinItems(
                                                        coinItemList,
                                                        refresh);
                                            } else if (coins.getCoinItems() != null && coins.getCoinItems().size() > 0) {
                                                favouritesView.addCoinItems(
                                                        coins.getCoinItems());
                                            }
                                        }
                                    }, throwable -> {
                                        if (favouritesView.isAttached()) {
                                            favouritesView.hideLoading();

                                            if (!favouritesView.notAuthorized(throwable)) {
                                                if (NetworkUtils.hasActiveNetworkConnection(
                                                        favouritesView.getActivity())) {
                                                    String message = ErrorUtils.getErrorMessage(
                                                            throwable,
                                                            favouritesView.getActivity().getApplicationContext());
                                                    favouritesView.showCustomAlertDialogSimple(
                                                            favouritesView.getActivity().getString(
                                                                    R.string.title_coins_failed),
                                                            message,
                                                            R.string.cancel,
                                                            R.string.retry,
                                                            view -> {
                                                                // Auto-dismissed
                                                            },
                                                            view -> fetchCoinItems(
                                                                    refresh,
                                                                    0),
                                                            DialogUtils.AlertType.NONE);
                                                } else {
                                                    boolean offlineData = favouritesView.getCoinItems() != null && favouritesView.getCoinItems().size() > 0;
                                                    Timber.d(
                                                            "offlineData: %b",
                                                            offlineData);
                                                    if (!offlineData) {
                                                        favouritesView.showNetworkErrorLayout(
                                                                () -> fetchCoinItems(
                                                                        refresh,
                                                                        0));
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
        if (favouritesView.isAttached()) {
            Intent intent = new Intent(favouritesView.getActivity(), CoinDetailActivity.class);
            intent.putExtra(Constants.INTENT_COIN_ITEM, Parcels.wrap(coinItem));

            // Do not make a scene transition if the user does not have a network connection
            // we do not know at this point if the profile to be loaded is available offline
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !NetworkUtils.hasActiveNetworkConnection(
                    favouritesView.getActivity().getApplicationContext())) {
                favouritesView.getActivity().startActivity(intent);
            } else {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        favouritesView.getActivity(),
                        transitionView,
                        favouritesView.getActivity().getResources().getString(R.string.shared_image));
                MainApplication.getBitmapCache().delBitmap();
                MainApplication.getBitmapCache().putBitmap(imageBitmap);
                favouritesView.getFragment().startActivityForResult(intent, Constants.INTENT_REQUEST_CODE_RETURNED_COIN_ITEM, options.toBundle());
            }
        }
    }

    private Coins manipulateCoinItems(Coins coins) {
        Timber.d("Manipulate Coin Items: %s", coins.getCoinItems());
        Timber.d("Transform Thread: %s", Thread.currentThread());

        // Remove any duplicates
        if (favouritesView.getCoinItems() != null && favouritesView.getCoinItems().size() > 0) {
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
            RealmList<CoinItem> sortedList = sortCoinItems(results);
            coins.setCoinItems(sortedList);
            coinItemList.addAll(sortedList);
        } else {
            coinItemList = coins.getCoinItems() != null ? sortCoinItems(coins.getCoinItems()) : new RealmList<>();
        }
        return coins;
    }

    private RealmList<CoinItem> sortCoinItems(RealmList<CoinItem> originalList) {
        // Sort the list by rank
        Collections.sort(originalList, (coinItem1, coinItem2) -> {
            if (coinItem1 == null || coinItem2 == null) {
                return 0;
            }

            if (TextUtils.isEmpty(coinItem1.getRank()) || TextUtils.isEmpty(coinItem2.getRank())) {
                return 0;
            }

            return Integer.parseInt(coinItem1.getRank()) > Integer.parseInt(coinItem2.getRank()) ? 1 : -1;
        });
        return originalList;
    }
}
