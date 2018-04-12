package com.lupinemoon.favicoin.presentation.ui.features.coindetail;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.lupinemoon.favicoin.MainApplication;
import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.databinding.ActivityCoinDetailBinding;
import com.lupinemoon.favicoin.presentation.ui.base.BaseVMPActivity;
import com.lupinemoon.favicoin.presentation.ui.base.BaseViewModel;
import com.lupinemoon.favicoin.presentation.utils.AnimationUtils;
import com.lupinemoon.favicoin.presentation.utils.Constants;
import com.lupinemoon.favicoin.presentation.utils.DialogUtils;
import com.lupinemoon.favicoin.presentation.utils.NetworkUtils;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.GenericCallback;

import org.parceler.Parcels;

import timber.log.Timber;

public class CoinDetailActivity extends BaseVMPActivity<CoinDetailContract.ViewModel, CoinDetailContract.Presenter, ActivityCoinDetailBinding> implements CoinDetailContract.View {

    private boolean loaded = false;

    private Bitmap coinImage;

    private boolean transitionDone = false;

    private CoinItem coinItem;

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

        // Do not make a scene transition if the user does not have a network connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && NetworkUtils.hasActiveNetworkConnection(
                getApplicationContext())) {
            coinImage = MainApplication.getBitmapCache().getBitmap();
            try {
                Transition transition = TransitionInflater.from(this).
                        inflateTransition(R.transition.accelerate_transform);
                transition.setDuration(250);
                transition.setInterpolator(new AccelerateInterpolator());
                getWindow().setExitTransition(transition);
            } catch (Exception e) {
                Timber.d("Get Extras Exception %s", e.toString());
            }

            getBinding().coinImageView.setImageBitmap(coinImage);
            MainApplication.getBitmapCache().delBitmap();

            getWindow().getEnterTransition().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {

                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    transitionDone = true;
                    postTransition();
                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });
        } else {
            AnimationUtils.animateTransitionStartActivity(
                    this.getActivity(),
                    R.anim.slide_in_right_fade_in);
            transitionDone = true;
            postTransition();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_coin_detail);
            if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(
                    Constants.INTENT_COIN_ITEM)) {
                coinItem = Parcels.unwrap(getIntent().getExtras().getParcelable(Constants.INTENT_COIN_ITEM));
                getSupportActionBar().setTitle(coinItem.getName());
            }
        }

        getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateOnBackPressed(new GenericCallback() {
                    @Override
                    public void execute() {
                        onBackPressed();
                    }
                });
            }
        });

        // Set the view model variable
        getBinding().setViewModel((CoinDetailViewModel) getViewModel());
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();

        // Do not make a scene transition if the user does not have a network connection
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !NetworkUtils.hasActiveNetworkConnection(
                getApplicationContext())) {
            AnimationUtils.animateTransitionFinishActivity(
                    this.getActivity(),
                    R.anim.slide_out_right_fade_out);
        } else {
            if (coinImage != null) {
                getBinding().coinImageView.setImageBitmap(
                        coinImage);
            } else {
                getBinding().coinImageView.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }

    @Override
    public void favouriteToggleSuccess(CoinItem coinItem) {
        getBinding().getViewModel().setCoinItem(coinItem);
    }

    private void toggleNoNetworkView(boolean hide) {
        View noNetworkView = getBinding().noNetworkLayout.getRoot();
        noNetworkView.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    private void postTransition() {
        if (transitionDone) {
            Timber.d("Transition complete");
            setCoinItem(coinItem);
        }
    }

    private void setCoinItem(CoinItem coinItem) {
        if (coinItem != null) {
            toggleNoNetworkView(true);
            loaded = true;
            // Set the view model variable
            getBinding().setViewModel((CoinDetailViewModel) getViewModel());
            getBinding().getViewModel().setCoinItem(coinItem);
        } else {
            showNoCoinItemDialog();
        }
    }

    @Override
    public void showLoading() {
        toggleNoNetworkView(true);
        if (!loaded) {
            getBinding().coinDetailBodyContainer.setVisibility(View.GONE);
        }
        togglePopupLoader(true);
    }

    @Override
    public void hideLoading() {
        // show recProfile
        getBinding().coinDetailBodyContainer.setVisibility(View.VISIBLE);
        togglePopupLoader(false);
    }

    @Override
    public void showNoCoinItemDialog() {
        showCustomAlertDialogSimple(
                R.string.title_no_coin_item,
                R.string.message_no_coin_item,
                R.string.ok,
                0,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Auto-dismissed
                        CoinDetailActivity.this.supportFinishAfterTransition();
                    }
                },
                null,
                DialogUtils.AlertType.NONE);
    }

}
