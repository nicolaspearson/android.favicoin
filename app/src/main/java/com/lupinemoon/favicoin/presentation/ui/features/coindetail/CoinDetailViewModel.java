package com.lupinemoon.favicoin.presentation.ui.features.coindetail;

import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.presentation.ui.base.BaseViewModel;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;
import com.lupinemoon.favicoin.presentation.utils.ImageUtils;

import org.parceler.Parcels;

import timber.log.Timber;

public class CoinDetailViewModel extends BaseViewModel implements CoinDetailContract.ViewModel {

    private CoinDetailContract.View coinDetailView;

    private CoinItem coinItem;

    CoinDetailViewModel(@NonNull CoinDetailContract.View view, @Nullable State savedInstanceState) {
        // Set the view locally
        coinDetailView = ActivityUtils.checkNotNull(view, "view cannot be null!");

        if (savedInstanceState != null && savedInstanceState instanceof CoinDetailState) {
            CoinDetailState coinDetailState = (CoinDetailState) savedInstanceState;
            // Restore local variable from saved state
            coinItem = coinDetailState.coinItem;
        }
    }

    public void setCoinItem(CoinItem coinItem) {
        this.coinItem = coinItem;
        notifyChange();
    }

    @Override
    public State getInstanceState() {
        return new CoinDetailState(this);
    }

    @Bindable
    public String getName() {
        return coinItem != null && coinItem.getName() != null ? coinItem.getName() : "";
    }

    @Bindable
    public String getSymbol() {
        return coinItem != null && coinItem.getSymbol() != null ? coinItem.getSymbol() : "";
    }

    @Bindable
    public String getRank() {
        if (coinItem != null && coinItem.getRank() != null) {
            return coinItem.getRank();
        }
        return "";
    }

    @Bindable
    public String getPriceUsd() {
        if (coinItem != null && coinItem.getPriceUsd() != null) {
            return String.format(
                    coinDetailView.getActivity().getString(R.string.dollar_format),
                    coinItem.getPriceUsd());
        }
        return "";
    }

    @Bindable
    public String getMarketCapUsd() {
        if (coinItem != null && coinItem.getMarketCapUsd() != null) {
            return String.format(
                    coinDetailView.getActivity().getString(R.string.dollar_format),
                    coinItem.getMarketCapUsd());
        }
        return "";
    }

    @Bindable
    public String getPercentChange24h() {
        if (coinItem != null && coinItem.getPercentChange24h() != null) {
            return String.format(
                    coinDetailView.getActivity().getString(R.string.percentage_format),
                    coinItem.getPercentChange24h());
        }
        return "";
    }

    @Bindable
    public Double getPercentChange24hValue() {
        if (coinItem != null && coinItem.getPercentChange24h() != null) {
            try {
                return Double.parseDouble(coinItem.getPercentChange24h());
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return 0D;
    }

    public void onFavouriteClick() {
        Timber.d("Favourite Clicked");
    }

    @Bindable
    private String getImageUrl() {
        return coinItem != null && coinItem.getImageUrl() != null ? coinItem.getImageUrl() : "";
    }

    @BindingAdapter({"loadCoinImage"})
    public static void loadCoinImage(
            final ImageView imageView,
            final CoinDetailViewModel coinDetailViewModel) {

        // ScaleType needs to be set when not using shared element transition
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        if (!TextUtils.isEmpty(coinDetailViewModel.getImageUrl())) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.ic_placeholder)
                    .dontAnimate()
                    .encodeFormat(Bitmap.CompressFormat.PNG)
                    .encodeQuality(100)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);

            Glide.with(imageView.getContext())
                    .applyDefaultRequestOptions(options)
                    .asBitmap()
                    .load(ImageUtils.getFullCoinUrl(
                            coinDetailViewModel.getImageUrl()))
                    .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(
                                @NonNull Bitmap resource,
                                @Nullable Transition<? super Bitmap> transition) {
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            imageView.setImageBitmap(resource);
                        }
                    });
        } else {
            // Fail over
            if (!ImageUtils.hasImage(imageView)) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }

    private static class CoinDetailState extends State {

        final CoinItem coinItem;

        CoinDetailState(CoinDetailViewModel viewModel) {
            super(viewModel);
            coinItem = viewModel.coinItem;
        }

        CoinDetailState(Parcel in) {
            super(in);
            coinItem = Parcels.unwrap(in.readParcelable(CoinItem.class.getClassLoader()));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(Parcels.wrap(coinItem), 0);
        }

        @SuppressWarnings("unused")
        public static final Creator<CoinDetailState> CREATOR = new Creator<CoinDetailViewModel.CoinDetailState>() {
            @Override
            public CoinDetailViewModel.CoinDetailState createFromParcel(Parcel in) {
                return new CoinDetailViewModel.CoinDetailState(in);
            }

            @Override
            public CoinDetailViewModel.CoinDetailState[] newArray(int size) {
                return new CoinDetailViewModel.CoinDetailState[size];
            }
        };
    }
}
