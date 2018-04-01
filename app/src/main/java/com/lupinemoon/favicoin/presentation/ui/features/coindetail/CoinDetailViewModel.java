package com.lupinemoon.favicoin.presentation.ui.features.coindetail;

import android.databinding.Bindable;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.android.databinding.library.baseAdapters.BR;
import com.lupinemoon.favicoin.presentation.ui.base.BaseViewModel;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;

public class CoinDetailViewModel extends BaseViewModel implements CoinDetailContract.ViewModel {

    private CoinDetailContract.View coinDetailView;

    private String coinDetailString;

    CoinDetailViewModel(@NonNull CoinDetailContract.View view, @Nullable State savedInstanceState) {
        // Set the view locally
        coinDetailView = ActivityUtils.checkNotNull(view, "view cannot be null!");

        if (savedInstanceState != null && savedInstanceState instanceof CoinDetailState) {
            CoinDetailState coinDetailState = (CoinDetailState) savedInstanceState;
            // Restore local variable from saved state
            coinDetailString = coinDetailState.coinDetailString;
        }
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
