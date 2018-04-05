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
import com.lupinemoon.favicoin.presentation.utils.DateTimeUtils;
import com.lupinemoon.favicoin.presentation.utils.ImageUtils;
import com.lupinemoon.favicoin.presentation.utils.NumberUtils;
import com.lupinemoon.favicoin.presentation.widgets.Toasty;

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
        return coinItem != null && coinItem.getName() != null ? coinItem.getName() : "N/A";
    }

    @Bindable
    public String getSymbol() {
        return coinItem != null && coinItem.getSymbol() != null ? coinItem.getSymbol() : "N/A";
    }

    @Bindable
    public String getRank() {
        if (coinItem != null && coinItem.getRank() != null) {
            return coinItem.getRank();
        }
        return "N/A";
    }

    @Bindable
    public String getPriceUsd() {
        if (coinItem != null && coinItem.getPriceUsd() != null) {
            return String.format(coinDetailView.getActivity().getString(R.string.dollar_format),
                    NumberUtils.formatNumberWithSpaces(coinItem.getPriceUsd()));
        }
        return "N/A";
    }

    @Bindable
    public String getPriceBtc() {
        return coinItem != null && coinItem.getPriceBtc() != null ? coinItem.getPriceBtc() : "N/A";
    }

    @Bindable
    public String getVolume24hUsd() {
        return coinItem != null && coinItem.get24hVolumeUsd() != null
                ? NumberUtils.formatNumberWithSpaces(coinItem.get24hVolumeUsd())
                : "N/A";
    }

    @Bindable
    public String getMarketCapUsd() {
        if (coinItem != null && coinItem.getMarketCapUsd() != null) {
            return String.format(coinDetailView.getActivity().getString(R.string.dollar_format),
                    NumberUtils.formatNumberWithSpaces(coinItem.getMarketCapUsd()));
        }
        return "N/A";
    }

    @Bindable
    public String getAvailableSupply() {
        return coinItem != null && coinItem.getAvailableSupply() != null
                ? NumberUtils.formatNumberWithSpaces(coinItem.getAvailableSupply())
                : "N/A";
    }

    @Bindable
    public String getTotalSupply() {
        return coinItem != null && coinItem.getTotalSupply() != null
                ? NumberUtils.formatNumberWithSpaces(coinItem.getTotalSupply())
                : "N/A";
    }

    @Bindable
    public String getMaxSupply() {
        return coinItem != null && coinItem.getMaxSupply() != null
                ? NumberUtils.formatNumberWithSpaces(coinItem.getMaxSupply())
                : "N/A";
    }

    @Bindable
    public String getPercentChange1h() {
        if (coinItem != null && coinItem.getPercentChange1h() != null) {
            return String.format(coinDetailView.getActivity().getString(R.string.percentage_format),
                    coinItem.getPercentChange1h());
        }
        return "N/A";
    }

    @Bindable
    public Double getPercentChange1hValue() {
        if (coinItem != null && coinItem.getPercentChange1h() != null) {
            try {
                return Double.parseDouble(coinItem.getPercentChange1h());
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return 0D;
    }

    @Bindable
    public String getPercentChange24h() {
        if (coinItem != null && coinItem.getPercentChange24h() != null) {
            return String.format(coinDetailView.getActivity().getString(R.string.percentage_format),
                    coinItem.getPercentChange24h());
        }
        return "N/A";
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

    @Bindable
    public String getPercentChange7d() {
        if (coinItem != null && coinItem.getPercentChange7d() != null) {
            return String.format(coinDetailView.getActivity().getString(R.string.percentage_format),
                    coinItem.getPercentChange7d());
        }
        return "N/A";
    }

    @Bindable
    public Double getPercentChange7dValue() {
        if (coinItem != null && coinItem.getPercentChange7d() != null) {
            try {
                return Double.parseDouble(coinItem.getPercentChange7d());
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return 0D;
    }

    @Bindable
    public String getLastUpdated() {
        if (coinItem != null && coinItem.getLastUpdated() != null) {
            try {
                return DateTimeUtils.epochToDateTimeString(coinItem.getLastUpdated());
            } catch (NumberFormatException e) {
                Timber.e(e, "Could not format last updated epoch");
            }
        }
        return "N/A";
    }

    @Bindable
    public String getAlgorithm() {
        return coinItem != null && coinItem.getCryptoCompareCoin() != null
                && coinItem.getCryptoCompareCoin().getAlgorithm() != null
                        ? coinItem.getCryptoCompareCoin().getAlgorithm()
                        : "N/A";
    }

    @Bindable
    public String getProofType() {
        return coinItem != null && coinItem.getCryptoCompareCoin() != null
                && coinItem.getCryptoCompareCoin().getProofType() != null
                        ? coinItem.getCryptoCompareCoin().getProofType()
                        : "N/A";
    }

    @Bindable
    public String getFullyPremined() {
        return coinItem != null && coinItem.getCryptoCompareCoin() != null
                && coinItem.getCryptoCompareCoin().getFullyPremined() != null
                        ? coinItem.getCryptoCompareCoin().getFullyPremined()
                        : "N/A";
    }

    @Bindable
    public String getPreminedValue() {
        return coinItem != null && coinItem.getCryptoCompareCoin() != null
                && coinItem.getCryptoCompareCoin().getPreMinedValue() != null
                        ? coinItem.getCryptoCompareCoin().getPreMinedValue()
                        : "N/A";
    }

    @Bindable
    private Boolean getIsFavourite() {
        return coinItem != null && coinItem.isFavourite();
    }

    public void onFavouriteClick() {
        Timber.d("Favourite Clicked");
        if (coinItem != null && !TextUtils.isEmpty(coinItem.getName())) {
            String message = String.format(coinDetailView.getActivity().getString(R.string.adding_to_favourites),
                    coinItem.getSymbol());
            coinDetailView.showToastMsg(message, Toasty.ToastType.INFO);
            coinDetailView.getPresenter().toggleFavourite(coinItem);
        }
    }

    @Bindable
    private String getImageUrl() {
        return coinItem != null && coinItem.getImageUrl() != null ? coinItem.getImageUrl() : "";
    }

    @BindingAdapter({ "loadCoinImage" })
    public static void loadCoinImage(final ImageView imageView, final CoinDetailViewModel coinDetailViewModel) {

        // ScaleType needs to be set when not using shared element transition
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        if (!TextUtils.isEmpty(coinDetailViewModel.getImageUrl())) {
            RequestOptions options = new RequestOptions().placeholder(R.drawable.ic_placeholder).dontAnimate()
                    .encodeFormat(Bitmap.CompressFormat.PNG).encodeQuality(100).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);

            Glide.with(imageView.getContext()).applyDefaultRequestOptions(options).asBitmap()
                    .load(ImageUtils.getFullCoinUrl(coinDetailViewModel.getImageUrl()))
                    .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource,
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

    @BindingAdapter(value = { "loadFavouriteImage" })
    public static void loadFavouriteImage(final ImageView imageView, final CoinDetailViewModel coinDetailViewModel) {
        imageView.setImageResource(
                coinDetailViewModel.getIsFavourite() ? R.drawable.vd_star : R.drawable.vd_star_outline);
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
