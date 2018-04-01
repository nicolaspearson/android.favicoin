package com.lupinemoon.favicoin.presentation.ui.features.landing.home.adapters;

import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.databinding.ListItemCoinBinding;
import com.lupinemoon.favicoin.presentation.ui.base.BaseViewModel;
import com.lupinemoon.favicoin.presentation.ui.features.landing.home.HomeContract;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;
import com.lupinemoon.favicoin.presentation.utils.ImageUtils;

import timber.log.Timber;

public class CoinItemViewModel extends BaseViewModel {

    private HomeContract.View homeView;

    private CoinItem coinItem;

    private ListItemCoinBinding listItemCoinBinding;

    private RequestBuilder requestBuilder;

    private Bitmap imageBitmap;

    private long lastClickTime = 0;

    CoinItemViewModel(
            HomeContract.View view,
            @NonNull RequestBuilder requestBuilder,
            ListItemCoinBinding listItemCoinBinding) {
        // Set the view locally
        homeView = ActivityUtils.checkNotNull(view, "view cannot be null!");
        this.requestBuilder = requestBuilder;
        this.listItemCoinBinding = listItemCoinBinding;
    }

    void setCoinItem(CoinItem coinItem) {
        this.coinItem = coinItem;
        notifyChange();
    }

    @Override
    public State getInstanceState() {
        // Not required
        return null;
    }

    // Checks for a double click
    private boolean doubleClick() {
        boolean result = false;
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            result = true;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        return result;
    }

    public void onCoinItemClicked() {
        if ((homeView.getActivity() != null) && !doubleClick()) {
            homeView.getPresenter().showCoinDetailView(
                    coinItem,
                    listItemCoinBinding.coinItemImageView,
                    getImageBitmap());
        }
    }

    @Bindable
    public String getName() {
        return coinItem.getName();
    }

    @Bindable
    public String getRank() {
        return coinItem.getRank();
    }

    @Bindable
    public String getMarketCapUsd() {
        return coinItem.getMarketCapUsd();
    }

    @Bindable
    public String getPercentChange24h() {
        return coinItem.getPercentChange24h();
    }

    @Bindable
    public String getImageUrl() {
        return coinItem.getImageUrl();
    }

    @Bindable
    public RequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    private Bitmap getImageBitmap() {
        return imageBitmap;
    }

    private void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    @BindingAdapter(value = {"loadCoinImage"})
    public static void loadCoinImage(
            final ImageView imageView,
            final CoinItemViewModel coinItemViewModel) {

        // Reset the coin bitmap
        coinItemViewModel.setImageBitmap(null);

        if (!TextUtils.isEmpty(coinItemViewModel.getImageUrl())) {
            coinItemViewModel.getRequestBuilder().load(ImageUtils.getFullCoinUrl(coinItemViewModel.getImageUrl()))
                    .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {

                        @Override
                        public void onResourceReady(
                                @NonNull Bitmap resource,
                                @Nullable Transition<? super Bitmap> transition) {
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            Timber.d("Height: %d", resource.getHeight());
                            Timber.d("Width: %d", resource.getWidth());

                            imageView.setImageBitmap(ImageUtils.getBitmapClippedCircle(resource));
                            coinItemViewModel.setImageBitmap(resource);
                        }
                    });
        }

        if (!ImageUtils.hasImage(imageView) || TextUtils.isEmpty(coinItemViewModel.getImageUrl())) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(R.drawable.ic_placeholder);
        }
    }
}