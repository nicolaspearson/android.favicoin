package com.lupinemoon.favicoin.presentation.ui.features.coindetail;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.databinding.ActivityCoinDetailBinding;
import com.lupinemoon.favicoin.presentation.ui.base.BaseVMPActivity;
import com.lupinemoon.favicoin.presentation.ui.base.BaseViewModel;

public class CoinDetailActivity extends BaseVMPActivity<CoinDetailContract.ViewModel, CoinDetailContract.Presenter, ActivityCoinDetailBinding> implements CoinDetailContract.View {

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_coin_detail;
    }

    @Override
    protected int getAnalyticsNameResource() {
        return R.string.app_name;
    }

    @Override
    protected boolean monitorOfflineMode() {
        return false;
    }

    @Nullable
    @Override
    public CoinDetailViewModel createViewModel(@Nullable BaseViewModel.State savedViewModelState) {
        // Create the view model
        return new CoinDetailViewModel(this, savedViewModelState);
    }

    @Nullable
    @Override
    public CoinDetailPresenter createPresenter(@NonNull Bundle args) {
        // Create the presenter
        return new CoinDetailPresenter(this, args);
    }

    @Nullable
    @Override
    public ActivityCoinDetailBinding createBinding(@NonNull Activity activity, int layoutId) {
        return DataBindingUtil.setContentView(activity, layoutId);
    }

    @Override
    public ActivityCoinDetailBinding getBinding() {
        return super.getBinding();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_coin_detail);
        }

        // Set the view model variable
        getBinding().setViewModel((CoinDetailViewModel) getViewModel());
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

}
