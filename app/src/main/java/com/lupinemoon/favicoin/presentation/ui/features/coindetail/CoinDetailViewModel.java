package com.lupinemoon.favicoin.presentation.ui.features.coindetail;

import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapEncoder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.lupinemoon.favicoin.BR;
import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.presentation.ui.base.BaseViewModel;
import com.lupinemoon.favicoin.presentation.ui.features.home.adapters.CoinItemViewModel;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;
import com.lupinemoon.favicoin.presentation.utils.ImageUtils;

import timber.log.Timber;

public class CoinDetailViewModel extends BaseViewModel implements CoinDetailContract.ViewModel {

    private CoinDetailContract.View coinDetailView;

    private String coinDetailString;

    private CoinItem coinItem;

    CoinDetailViewModel(@NonNull CoinDetailContract.View view, @Nullable State savedInstanceState) {
        // Set the view locally
        coinDetailView = ActivityUtils.checkNotNull(view, "view cannot be null!");

        if (savedInstanceState != null && savedInstanceState instanceof CoinDetailState) {
            CoinDetailState coinDetailState = (CoinDetailState) savedInstanceState;
            // Restore local variable from saved state
            coinDetailString = coinDetailState.coinDetailString;
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

    public void onCoinDetailClick(View view) {
        if (validate(coinDetailString) && getError(coinDetailString, "Error") == null) {
            coinDetailView.getPresenter().performAction(coinDetailString);
        }
    }

    @Bindable
    public String getCoinDetailString() {
        return coinDetailString;
    }

    public void setCoinDetailString(String coinDetailString) {
        this.coinDetailString = coinDetailString;
        notifyPropertyChanged(BR.coinDetailString);
    }


    public void onFavouriteClick() {
        if (validate(coinDetailString) && getError(coinDetailString, "Error") == null) {
            coinDetailView.getPresenter().performAction(coinDetailString);
        }
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
                            // imageView.setImageBitmap(ImageUtils.getBitmapClippedCircle(resource));
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

        final String coinDetailString;

        CoinDetailState(CoinDetailViewModel viewModel) {
            super(viewModel);
            coinDetailString = viewModel.coinDetailString;
        }

        CoinDetailState(Parcel in) {
            super(in);
            coinDetailString = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(coinDetailString);
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
