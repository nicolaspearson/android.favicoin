package com.lupinemoon.favicoin.presentation.ui.features.offline;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.databinding.ActivityOfflineBinding;
import com.lupinemoon.favicoin.presentation.ui.base.BaseVMPActivity;
import com.lupinemoon.favicoin.presentation.ui.base.BaseViewModel;
import com.lupinemoon.favicoin.presentation.utils.AnimationUtils;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.GenericCallback;

public class OfflineActivity extends BaseVMPActivity<OfflineContract.ViewModel, OfflineContract.Presenter, ActivityOfflineBinding> implements OfflineContract.View {

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_offline;
    }

    @Override
    protected int getAnalyticsNameResource() {
        return R.string.analytics_offline;
    }

    @Override
    protected boolean monitorOfflineMode() {
        return false;
    }

    @Nullable
    @Override
    public OfflineViewModel createViewModel(@Nullable BaseViewModel.State savedViewModelState) {
        // Create the view model
        return new OfflineViewModel(this, savedViewModelState);
    }

    @Nullable
    @Override
    public OfflinePresenter createPresenter(@NonNull Bundle args) {
        // Create the presenter
        return new OfflinePresenter(this, args);
    }

    @Nullable
    @Override
    public ActivityOfflineBinding createBinding(@NonNull Activity activity, int layoutId) {
        return DataBindingUtil.setContentView(activity, layoutId);
    }

    @Override
    public ActivityOfflineBinding getBinding() {
        return super.getBinding();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AnimationUtils.animateTransitionStartActivity(
                this.getActivity(),
                R.anim.slide_in_right_fade_in);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_offline);
        }

        getToolbar().setNavigationOnClickListener(view -> animateOnBackPressed(() -> onBackPressed()));

        // Set the view model variable
        getBinding().setViewModel((OfflineViewModel) getViewModel());
    }

    @Override
    public void onBackPressed() {
        closeOfflineActivity();
    }

    @Override
    public void closeOfflineActivity() {
        supportFinishAfterTransition();
        AnimationUtils.animateTransitionFinishActivity(
                this.getActivity(),
                R.anim.slide_out_right_fade_out);
    }
}
