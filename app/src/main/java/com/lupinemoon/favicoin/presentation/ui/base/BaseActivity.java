package com.lupinemoon.favicoin.presentation.ui.base;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.data.analytics.AnalyticsService;
import com.lupinemoon.favicoin.presentation.services.rxbus.RxBus;
import com.lupinemoon.favicoin.presentation.services.rxbus.events.NetworkRestoredEvent;
import com.lupinemoon.favicoin.presentation.services.rxbus.events.QueueProcessingComplete;
import com.lupinemoon.favicoin.presentation.services.rxbus.events.QueueProcessingStarted;
import com.lupinemoon.favicoin.presentation.ui.features.offline.OfflineActivity;
import com.lupinemoon.favicoin.presentation.utils.Constants;
import com.lupinemoon.favicoin.presentation.utils.DialogUtils;
import com.lupinemoon.favicoin.presentation.utils.ErrorUtils;
import com.lupinemoon.favicoin.presentation.utils.NetworkUtils;
import com.lupinemoon.favicoin.presentation.utils.RetrofitUtils;
import com.lupinemoon.favicoin.presentation.utils.ViewUtils;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.OnBackPressedListener;
import com.lupinemoon.favicoin.presentation.widgets.PopupLoader;
import com.lupinemoon.favicoin.presentation.widgets.Toasty;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.GenericCallback;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static com.lupinemoon.favicoin.presentation.utils.NetworkUtils.hasActiveNetworkConnection;

public abstract class BaseActivity<B extends ViewDataBinding> extends AppCompatActivity implements IBaseView {

    protected boolean destroyedCompat = false;

    protected OnBackPressedListener onBackPressedListener;

    private View rootView;

    private Toolbar toolbar;

    private BroadcastReceiver networkStateReceiver;

    private B binding;

    protected CompositeDisposable rxBusEvents;

    private AnalyticsService analyticsService = AnalyticsService.getInstance();

    private boolean loading = false;

    private AlertDialog customAlertDialog = null;

    protected abstract int getContentViewResource();

    protected abstract int getAnalyticsNameResource();

    protected abstract boolean monitorOfflineMode();

    @Nullable
    public abstract B createBinding(@NonNull Activity activity, int layoutId);

    public B getBinding() {
        return binding;
    }

    public void setBinding(B binding) {
        this.binding = binding;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rxBusEvents = new CompositeDisposable();

        createRxBusSubscriptions();

        setBinding(createBinding(this, getContentViewResource()));

        // Set up the toolbar.
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            configureOfflineLayout();
        }

        rootView = getWindow().getDecorView().getRootView();
        destroyedCompat = false;

        if (monitorOfflineMode() && networkStateReceiver != null) {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(networkStateReceiver, filter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getAnalyticsNameResource() != 0) {
            analyticsService.onViewStart(
                    this.getResources().getString(getAnalyticsNameResource()));
        }
    }

    @Override
    public void onDetachedFromWindow() {
        togglePopupLoader(false);
        super.onDetachedFromWindow();
    }

    @Override
    public void onDestroy() {
        destroyedCompat = true;
        togglePopupLoader(false);
        if (rxBusEvents != null) {
            rxBusEvents.clear();
            rxBusEvents = null;
        }

        if (networkStateReceiver != null) {
            unregisterReceiver(networkStateReceiver);
        }
        dismissCustomAlertDialog();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (onBackPressedListener != null) {
            onBackPressedListener.doOnBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    public boolean isBasicallyDestroyedCompat() {
        return destroyedCompat || this.isFinishing();
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    @Override
    public void animateOnBackPressed(final GenericCallback onBackPressedCallback) {
        // This doesn't animate, it just waits for the default animation to end
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            new Handler().postDelayed(
                    onBackPressedCallback::execute,
                    Constants.DEFAULT_SELECTOR_DELAY);
        } else {
            onBackPressedCallback.execute();
        }
    }

    public View getRootView() {
        return rootView;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public boolean isAttached() {
        return !this.isBasicallyDestroyedCompat();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void showLoading() {
        togglePopupLoader(true);
    }

    @Override
    public void hideLoading() {
        togglePopupLoader(false);
    }

    @Override
    public boolean isLoading() {
        return loading;
    }

    public void togglePopupLoader(final boolean show) {
        if (getRootView() == null) {
            return;
        }

        loading = show;

        Timber.d(
                "togglePopupLoader: %s, %s",
                this.getClass().getSimpleName(),
                String.valueOf(show));
        try {
            if (show) {
                if (!isBasicallyDestroyedCompat()) {
                    PopupLoader.showPopupLoader(getApplicationContext(), getRootView());
                }
            } else {
                PopupLoader.dismissPopupLoader(getApplicationContext());
            }
        } catch (Exception e) {
            Timber.e(e, e.toString());
        }
    }

    @Override
    public void showToastMsg(String msg, Toasty.ToastType toastType) {
        if (toastType == null) {
            Toasty.normal(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            return;
        }

        switch (toastType) {
            case INFO:
                Toasty.info(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                break;

            case ERROR:
                Toasty.error(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                break;

            case SUCCESS:
                Toasty.success(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                break;

            case WARNING:
                Toasty.warning(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                break;

            default:
                Toasty.normal(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void showSnackbarMsg(String msg, int duration) {
        if (!ViewUtils.isAttachedToWindow(rootView)) {
            return;
        }

        // Duration between LENGTH_SHORT & LENGTH_LONG
        final Snackbar snackBar = Snackbar.make(rootView, msg, duration);
        snackBar.setActionTextColor(Color.RED);
        snackBar.setAction(getString(R.string.snackbar_dismiss_action), v -> snackBar.dismiss());
        snackBar.show();
    }

    @Override
    public AlertDialog showCustomAlertDialogSimple(
            int title,
            int message,
            int posButton,
            int negButton,
            View.OnClickListener posButtonClick,
            View.OnClickListener negButtonClick,
            DialogUtils.AlertType alertType) {
        // This is used when passing a title and message from strings.xml
        return showCustomAlertDialogSimple(
                getString(title),
                getString(message),
                posButton,
                negButton,
                posButtonClick,
                negButtonClick,
                alertType);
    }

    @Override
    public AlertDialog showCustomAlertDialogSimple(
            String title,
            String message,
            int posButton,
            int negButton,
            View.OnClickListener posButtonClick,
            View.OnClickListener negButtonClick,
            DialogUtils.AlertType alertType) {
        return DialogUtils.showCustomAlertDialog(
                this,
                title,
                message,
                posButton,
                negButton,
                posButtonClick,
                negButtonClick,
                alertType == null ? DialogUtils.AlertType.NONE : alertType);
    }

    @Override
    public void showCustomRetryErrorDialog(
            Throwable throwable,
            final GenericCallback retryCallback,
            String title) {
        if (hasActiveNetworkConnection(getActivity().getApplicationContext())) {
            showCustomNetworkErrorDialog(retryCallback);
        } else {
            // Something else went wrong
            Timber.e(throwable, "showErrorDialog");
            String message = ErrorUtils.getErrorMessage(throwable, getApplicationContext());
            showCustomAlertDialogSimple(
                    title,
                    message,
                    R.string.cancel,
                    R.string.retry,
                    view -> {
                        // Auto-dismissed
                    },
                    view -> retryCallback.execute(),
                    DialogUtils.AlertType.NONE);
        }
    }

    @Override
    public void showCustomNetworkErrorDialog(final GenericCallback retryCallback) {
        // Show an error dialog that allows the user to retry the failed request.
        customAlertDialog = showCustomAlertDialogSimple(
                R.string.title_network_error,
                R.string.message_network_error,
                R.string.retry,
                R.string.cancel,
                view -> {
                    // Auto dismissed
                    retryCallback.execute();
                },
                view -> {
                    // Auto dismissed
                },
                DialogUtils.AlertType.NETWORK);
    }

    @Override
    public void showCustomNetworkErrorDialog(
            final GenericCallback retryCallback,
            final GenericCallback cancelCallback) {
        // Show an error dialog that allows the user to retry the failed request.
        customAlertDialog = showCustomAlertDialogSimple(
                R.string.title_network_error,
                R.string.message_network_error,
                R.string.retry,
                R.string.cancel,
                view -> {
                    // Auto dismissed
                    retryCallback.execute();
                },
                view -> cancelCallback.execute(),
                DialogUtils.AlertType.NETWORK);
    }

    @Override
    public boolean notAuthorized(Throwable throwable) {
        // Check if the returned status code is a 440 or 401
        // this means that the user does not have a valid
        // auth token (401) or is not subscribed (440)
        // which will be handled by the SubscriptionExpiredEvent
        return RetrofitUtils.isHttp401Error(throwable) || RetrofitUtils.isHttp440Error(throwable);
    }


    // To avoid memory leaks we dismiss any visible dialogs
    public void dismissCustomAlertDialog() {
        if (customAlertDialog != null) {
            try {
                customAlertDialog.dismiss();
            } catch (Exception e) {
                Timber.e(e, "An error occurred dismissing the dialog");
            }
        }
    }

    private void createRxBusSubscriptions() {
        // Queue processing has completed with or without errors, clear banner.
        rxBusEvents.add(
                RxBus.getDefault().observeEvents(QueueProcessingComplete.class)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(queueProcessingComplete -> {
                            ViewGroup offlineLayout = getRootView().findViewById(R.id.offline_banner_layout);
                            final Context context = getActivity();
                            if (offlineLayout != null && NetworkUtils.hasActiveNetworkConnection(
                                    context)) {
                                if (offlineLayout.findViewById(R.id.offline_banner_offline_container).getVisibility() != View.VISIBLE) {
                                    resetOfflineBanner(offlineLayout);
                                }
                            }
                        }));

        // Queue started, there are > 0 items. Show syncing banner.
        rxBusEvents.add(
                RxBus.getDefault().observeEvents(QueueProcessingStarted.class)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(queueProcessingStarted -> {
                            if (NetworkUtils.hasActiveNetworkConnection(getActivity())) {
                                final View offlineLayout = rootView.findViewById(R.id.offline_banner_layout);
                                if (offlineLayout != null && getActivity() != null && isAttached()) {
                                    offlineLayout.findViewById(R.id.offline_banner_offline_container).setVisibility(
                                            View.GONE);
                                    offlineLayout.findViewById(R.id.offline_banner_connected_container).setVisibility(
                                            View.VISIBLE);
                                    if (offlineLayout.getVisibility() == View.GONE) {
                                        offlineLayout.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }));
    }

    private void resetOfflineBanner(ViewGroup viewGroup) {
        viewGroup.setVisibility(View.GONE);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            viewGroup.getChildAt(i).setVisibility(View.GONE);
        }
    }

    private void configureOfflineLayout() {
        if (monitorOfflineMode()) {
            networkStateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Timber.d("onReceive: Network State Receiver");
                    evaluateConnectionState(getRootView());
                }
            };
        }
    }

    private void evaluateConnectionState(View rootView) {
        try {
            final View offlineLayout = rootView.findViewById(R.id.offline_banner_layout);
            if (monitorOfflineMode() && offlineLayout != null) {
                Timber.d("offlineLayout: %s", offlineLayout);

                // Configure the offline button
                View offlineButton = offlineLayout.findViewById(R.id.offline_more_button);
                if (offlineButton != null && !offlineButton.hasOnClickListeners()) {
                    Timber.d("Set offlineButton Click Listener");
                    offlineButton.setOnClickListener(v -> {
                        Intent offlineIntent = new Intent(
                                getActivity(),
                                OfflineActivity.class);
                        getActivity().startActivity(offlineIntent);
                    });
                }

                if (hasActiveNetworkConnection(rootView.getContext())) {
                    Timber.d("evaluateConnectionState: Connected");
                    RxBus.getDefault().post(new NetworkRestoredEvent());
                    if (getActivity() != null && isAttached()) {
                        Timber.d("Offline Banner: Hide");
                        if (offlineLayout.findViewById(R.id.offline_banner_offline_container).getVisibility() == View.VISIBLE) {
                            offlineLayout.findViewById(R.id.offline_banner_offline_container).setVisibility(
                                    View.GONE);
                        }
                    }
                } else {
                    Timber.d("evaluateConnectionState: Disconnected");
                    if (getActivity() != null && isAttached()) {
                        Timber.d("Offline Banner: Show");
                        if (offlineLayout.findViewById(R.id.offline_banner_connected_container).getVisibility() == View.VISIBLE) {
                            offlineLayout.findViewById(R.id.offline_banner_connected_container).setVisibility(
                                    View.GONE);
                        }
                        offlineLayout.setVisibility(View.VISIBLE);
                        offlineLayout.findViewById(R.id.offline_banner_offline_container).setVisibility(
                                View.VISIBLE);
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e, "evaluateConnectionState Error");
        }
    }
}
