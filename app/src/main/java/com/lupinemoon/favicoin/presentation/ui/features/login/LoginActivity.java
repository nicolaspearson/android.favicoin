package com.lupinemoon.favicoin.presentation.ui.features.login;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.databinding.ActivityLoginBinding;
import com.lupinemoon.favicoin.presentation.ui.base.BaseVMPActivity;
import com.lupinemoon.favicoin.presentation.ui.base.BaseViewModel;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;

import timber.log.Timber;

public class LoginActivity extends BaseVMPActivity<LoginContract.ViewModel, LoginContract.Presenter, ActivityLoginBinding> implements LoginContract.View {

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_login;
    }

    @Override
    protected int getAnalyticsNameResource() {
        return R.string.analytics_login;
    }

    @Override
    protected boolean monitorOfflineMode() {
        return false;
    }

    @Nullable
    @Override
    public LoginViewModel createViewModel(@Nullable BaseViewModel.State savedViewModelState) {
        // Create the view model
        return new LoginViewModel(this, savedViewModelState);
    }

    @Nullable
    @Override
    public LoginPresenter createPresenter(@NonNull Bundle args) {
        // Create the presenter
        return new LoginPresenter(this, args);
    }

    @Nullable
    @Override
    public ActivityLoginBinding createBinding(@NonNull Activity activity, int layoutId) {
        return DataBindingUtil.setContentView(activity, layoutId);
    }

    @Override
    public ActivityLoginBinding getBinding() {
        return super.getBinding();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the view model variable
        getBinding().setViewModel((LoginViewModel) getViewModel());

        intentReceived(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        intentReceived(intent);
    }

    private void intentReceived(Intent intent) {
        Timber.d("onNewIntent: %s", intent);
    }

    @Override
    public void onBackPressed() {
        ActivityUtils.minimizeApp(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getBinding().username.setText("");
        getBinding().password.setText("");
        getBinding().logoImageView.requestFocus();
    }
}
